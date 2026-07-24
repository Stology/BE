package com.stology.be.domain.inquiry.service;

import com.stology.be.domain.inquiry.converter.InquiryConverter;
import com.stology.be.domain.inquiry.dto.request.InquiryReqDTO;
import com.stology.be.domain.inquiry.dto.response.InquiryResDTO;
import com.stology.be.domain.inquiry.exception.InquiryErrorCode;
import com.stology.be.domain.inquiry.exception.InquiryException;
import com.stology.be.domain.inquiry.repository.InquiryReplyRepository;
import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.study.entity.Answer;
import com.stology.be.domain.study.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 답글(Answer) 기능: 작성/수정/삭제.
 * 엔티티 조회·권한 검증은 {@link FinderService}, 이미지 처리는 {@link ImageService}에 위임한다.
 *
 * <p>질문과 동일하게 S3 업로드는 트랜잭션 밖에서, DB 쓰기만 짧은 트랜잭션으로 처리한다.
 * 업로드 경로가 questionId(부모)만 쓰므로 answer insert 전에 업로드가 가능하다.
 */
@Service
@RequiredArgsConstructor
public class AnswerService {

    private final InquiryReplyRepository inquiryReplyRepository;
    private final FinderService finder;
    private final ImageService imageService;
    private final WriteTxService writeTx;

    /**
     * 답글 작성. (1) 검증·권한 확인 → (2) S3 업로드(트랜잭션 밖) → (3) 짧은 트랜잭션에서 DB 저장·토큰 치환.
     */
    public InquiryResDTO.WriteAnswerResult writeAnswer(Long studyId, Long questionId, Long memberId, InquiryReqDTO.WriteAnswer request, List<MultipartFile> images) {
        validateReplyContent(request.getContent());

        List<MultipartFile> files = imageService.nonEmptyImages(images);
        Question question = finder.getQuestionInStudy(studyId, questionId);
        finder.requireStudyMember(studyId, memberId);
        finder.requireStudyActive(finder.getStudy(studyId));   // 연관 탐색 대신 studyId로 직접 조회
        imageService.validateCreateTokens(request.getContent(), imageService.count(files));

        List<String> urls = imageService.uploadImages("answer/" + questionId, files);   // 트랜잭션 밖

        return writeTx.commitOrCompensate(urls, status -> {
            Question managedQuestion = finder.getQuestionInStudy(studyId, questionId);
            Member member = finder.getMember(memberId);
            Answer answer = InquiryConverter.toAnswer(request, managedQuestion, member);
            inquiryReplyRepository.save(answer);
            String finalContent = imageService.persistNewAnswerImages(answer, request.getContent(), urls);
            if (!finalContent.equals(request.getContent())) {
                answer.updateContent(finalContent);
            }
            managedQuestion.increaseAnswerCount();
            return new InquiryResDTO.WriteAnswerResult(answer.getId());
        });
    }

    /**
     * 답글 수정. content의 [[img:{imageId}]]는 유지, [[img:new:K]]는 추가이며,
     * content에서 빠진 기존 이미지는 삭제(DB + 커밋 후 S3)된다.
     */
    public InquiryResDTO.UpdateAnswerResult updateAnswer(Long studyId, Long questionId, Long answerId, Long memberId, InquiryReqDTO.UpdateAnswer request, List<MultipartFile> images) {
        validateReplyContent(request.getContent());

        List<MultipartFile> files = imageService.nonEmptyImages(images);
        Answer answer = finder.getAnswerInQuestion(studyId, questionId, answerId);
        Member member = finder.getMember(memberId);
        finder.requireAnswerOwner(answer, member);
        finder.requireStudyActive(finder.getStudy(studyId));   // 연관 탐색 대신 studyId로 직접 조회
        imageService.validateUpdateTokens(request.getContent(), imageService.count(files), imageService.answerImageIds(answerId));

        List<String> urls = imageService.uploadImages("answer/" + questionId, files);   // 트랜잭션 밖

        return writeTx.commitOrCompensate(urls, status -> {
            Answer managed = finder.getAnswerInQuestion(studyId, questionId, answerId);
            String finalContent = imageService.replaceAnswerImages(managed, request.getContent(), urls);
            managed.updateContent(finalContent);
            return new InquiryResDTO.UpdateAnswerResult(managed.getId());
        });
    }

    /** 답글 hard delete. 이미지 DB row + 커밋 후 S3 객체까지 실제 삭제한다(FK 때문에 이미지 → 답글 순). */
    @Transactional
    public void deleteAnswer(Long studyId, Long questionId, Long answerId, Long memberId) {
        Answer answer = finder.getAnswerInQuestion(studyId, questionId, answerId);
        Member member = finder.getMember(memberId);
        finder.requireAnswerOwner(answer, member);
        Question question = answer.getQuestion();
        finder.requireStudyActive(question.getStudy());

        imageService.deleteAnswerImages(answerId);
        inquiryReplyRepository.delete(answer);
        question.decreaseAnswerCount();
    }

    /**
     * 답글도 질문 본문과 동일하게 1자 이상 1000자 이하. [[img:N]] 토큰은 텍스트가 아니므로 길이에서 제외한다.
     */
    private void validateReplyContent(String content) {
        if (content == null || content.isBlank() || imageService.textLength(content) > 1000) {
            throw new InquiryException(InquiryErrorCode.REPLY_BODY_INVALID);
        }
    }
}
