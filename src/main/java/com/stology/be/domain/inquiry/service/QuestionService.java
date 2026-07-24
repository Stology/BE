package com.stology.be.domain.inquiry.service;

import com.stology.be.domain.inquiry.converter.InquiryConverter;
import com.stology.be.domain.inquiry.dto.request.InquiryReqDTO;
import com.stology.be.domain.inquiry.dto.response.InquiryResDTO;
import com.stology.be.domain.inquiry.exception.InquiryErrorCode;
import com.stology.be.domain.inquiry.exception.InquiryException;
import com.stology.be.domain.inquiry.repository.InquiryReplyRepository;
import com.stology.be.domain.inquiry.repository.InquiryRepository;
import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.study.entity.Answer;
import com.stology.be.domain.study.entity.Question;
import com.stology.be.domain.study.entity.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 질문(Question) 기능: 목록/상세 조회, 작성/수정/삭제.
 * 엔티티 조회·권한 검증은 {@link FinderService}, 이미지 처리는 {@link ImageService}에 위임한다.
 *
 * <p>작성/수정은 S3 업로드(느린 I/O)를 트랜잭션 밖에서 먼저 끝내고, DB 쓰기만 짧은 트랜잭션
 * ({@link WriteTxService})으로 처리한다. 업로드 경로가 부모 id(studyId)만 쓰므로 insert 전에 업로드가 가능하다.
 */
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final InquiryRepository inquiryRepository;
    private final InquiryReplyRepository inquiryReplyRepository;
    private final FinderService finder;
    private final ImageService imageService;
    private final WriteTxService writeTx;

    /** 페이지 크기 상한. 클라이언트가 과도한 size를 넘겨 대량 조회하는 것을 막는다. */
    private static final int MAX_PAGE_SIZE = 50;

    @Transactional(readOnly = true)
    public InquiryResDTO.QuestionList getQuestions(Long studyId, Integer page, Integer size, Long memberId) {
        Study study = finder.getStudy(studyId);
        finder.requireStudyMember(studyId, memberId);
        Member member = finder.getMember(memberId);

        int safePage = (page == null || page < 0) ? 0 : page;
        int safeSize = (size == null || size < 1) ? 10 : Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Question> questionPage = inquiryRepository.findByStudyIdAndDeletedAtIsNullOrderByCreatedAtDesc(studyId, pageable);

        return InquiryConverter.toQuestionList(questionPage, !study.getIsActive(), member.getName());
    }

    @Transactional(readOnly = true)
    public InquiryResDTO.QuestionDetail getQuestionDetail(Long studyId, Long questionId, Long memberId) {
        Study study = finder.getStudy(studyId);
        finder.requireStudyMember(studyId, memberId);
        Member member = finder.getMember(memberId);

        Question question = finder.getQuestionInStudy(studyId, questionId);
        List<InquiryResDTO.ImageInfo> images = imageService.getQuestionImages(questionId);

        List<Answer> answers = inquiryReplyRepository.findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(questionId);
        List<Long> answerIds = answers.stream().map(Answer::getId).collect(Collectors.toList());
        Map<Long, List<InquiryResDTO.ImageInfo>> imagesByAnswer = imageService.getAnswerImagesByAnswerIds(answerIds);

        List<InquiryResDTO.AnswerDetail> answerList = answers.stream()
                .map(answer -> InquiryConverter.toAnswerDetail(
                        answer, imagesByAnswer.getOrDefault(answer.getId(), List.of()), member.getName()))
                .collect(Collectors.toList());

        return InquiryConverter.toQuestionDetail(question, images, answerList, !study.getIsActive(), member.getName());
    }

    /**
     * 질문 작성. (1) 검증·권한 확인 → (2) S3 업로드(트랜잭션 밖) → (3) 짧은 트랜잭션에서 DB 저장·토큰 치환.
     * content의 [[img:new:K]]가 images의 K번째와 대응하며, 저장 후 실제 [[img:{imageId}]]로 치환된다.
     */
    public InquiryResDTO.WriteQuestionResult writeQuestion(Long studyId, Long memberId, InquiryReqDTO.WriteQuestion request, List<MultipartFile> images) {
        validateTitle(request.getTitle());
        validateContent(request.getContent());

        List<MultipartFile> files = imageService.nonEmptyImages(images);
        imageService.validateCreateTokens(request.getContent(), imageService.count(files));
        finder.getActiveStudyForMember(studyId, memberId);   // 권한/상태 확인(업로드 전)

        List<String> urls = imageService.uploadImages("question/" + studyId, files);   // 트랜잭션 밖

        return writeTx.commitOrCompensate(urls, status -> {
            Study study = finder.getStudy(studyId);
            Member member = finder.getMember(memberId);
            Question question = InquiryConverter.toQuestion(request, study, member, !urls.isEmpty());
            inquiryRepository.save(question);
            String finalContent = imageService.persistNewQuestionImages(question, request.getContent(), urls);
            if (!finalContent.equals(request.getContent())) {
                question.update(request.getTitle(), finalContent);
            }
            return new InquiryResDTO.WriteQuestionResult(question.getId());
        });
    }

    /**
     * 질문 수정. 업로드는 트랜잭션 밖에서, DB 반영만 트랜잭션 안에서 처리한다.
     * content의 [[img:{imageId}]]는 기존 이미지 유지, [[img:new:K]]는 새 파일 추가이며,
     * content에서 빠진 기존 이미지는 삭제(DB + 커밋 후 S3)된다.
     */
    public InquiryResDTO.UpdateQuestionResult updateQuestion(Long studyId, Long questionId, Long memberId, InquiryReqDTO.UpdateQuestion request, List<MultipartFile> images) {
        validateTitle(request.getTitle());
        validateContent(request.getContent());

        List<MultipartFile> files = imageService.nonEmptyImages(images);
        Question question = finder.getQuestionInStudy(studyId, questionId);
        Member member = finder.getMember(memberId);
        finder.requireQuestionOwner(question, member);
        finder.requireStudyActive(finder.getStudy(studyId));   // 연관 탐색 대신 studyId로 직접 조회
        imageService.validateUpdateTokens(request.getContent(), imageService.count(files), imageService.questionImageIds(questionId));

        List<String> urls = imageService.uploadImages("question/" + studyId, files);   // 트랜잭션 밖

        return writeTx.commitOrCompensate(urls, status -> {
            Question managed = finder.getQuestionInStudy(studyId, questionId);
            String finalContent = imageService.replaceQuestionImages(managed, request.getContent(), urls);
            managed.update(request.getTitle(), finalContent);
            managed.updateAttached(imageService.hasImageToken(finalContent));
            return new InquiryResDTO.UpdateQuestionResult(managed.getId());
        });
    }

    /**
     * 질문 hard delete. 답글/이미지까지 DB에서 실제 삭제하고 S3 객체는 커밋 이후 제거한다(FK 때문에 자식 → 부모 순).
     * 업로드가 없으므로 일반 트랜잭션으로 처리한다(삭제 S3 제거는 커밋 후로 미뤄져 커넥션을 오래 잡지 않는다).
     */
    @Transactional
    public void deleteQuestion(Long studyId, Long questionId, Long memberId) {
        Question question = finder.getQuestionInStudy(studyId, questionId);
        Member member = finder.getMember(memberId);
        finder.requireQuestionOwner(question, member);
        finder.requireStudyActive(question.getStudy());

        List<Answer> answers = inquiryReplyRepository.findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(questionId);
        List<Long> answerIds = answers.stream().map(Answer::getId).collect(Collectors.toList());
        imageService.deleteAnswerImagesByAnswerIds(answerIds);   // 답글별 조회 대신 IN 조회 1번
        inquiryReplyRepository.deleteAll(answers);

        imageService.deleteQuestionImages(questionId);
        inquiryRepository.delete(question);
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank() || title.length() > 50) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_TITLE_INVALID);
        }
    }

    /**
     * 본문의 [[img:N]]은 이미지 자리표시자일 뿐 사용자가 입력한 텍스트가 아니다.
     * 길이 제한(1000자)은 "본문 텍스트 기준"이므로 토큰을 제외하고 센다.
     * 다만 필수값 검사는 원본 기준이라, 이미지만 있고 글이 없는 본문도 통과한다.
     */
    private void validateContent(String content) {
        if (content == null || content.isBlank() || imageService.textLength(content) > 1000) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_BODY_INVALID);
        }
    }
}
