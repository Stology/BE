package com.stology.be.domain.inquiry.service;

import com.stology.be.domain.inquiry.converter.InquiryConverter;
import com.stology.be.domain.inquiry.dto.request.InquiryReqDTO;
import com.stology.be.domain.inquiry.dto.response.InquiryResDTO;
import com.stology.be.domain.inquiry.enums.InquiryStatus;
import com.stology.be.domain.inquiry.exception.InquiryErrorCode;
import com.stology.be.domain.inquiry.exception.InquiryException;
import com.stology.be.domain.inquiry.repository.InquiryReadRepository;
import com.stology.be.domain.inquiry.repository.InquiryReplyRepository;
import com.stology.be.domain.inquiry.repository.InquiryRepository;
import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.study.entity.Answer;
import com.stology.be.domain.study.entity.Question;
import com.stology.be.domain.study.entity.QuestionRead;
import com.stology.be.domain.study.entity.Study;
import com.stology.be.domain.study.repository.MemberStudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final InquiryRepository inquiryRepository;
    private final InquiryReplyRepository inquiryReplyRepository;
    private final InquiryReadRepository inquiryReadRepository;
    private final MemberStudyRepository memberStudyRepository;

    private final FinderService finder;
    private final ImageService imageService;
    private final WriteTxService writeTx;
    private final ReadService readService;

    /** 페이지 크기 상한. 클라이언트가 과도한 size를 넘겨 대량 조회하는 것을 막는다. */
    private static final int MAX_PAGE_SIZE = 50;

    @Transactional(readOnly = true)
    public InquiryResDTO.QuestionList getQuestions(Long studyId, Integer page, Integer size, Long memberId) {
        Study study = finder.getStudy(studyId);
        finder.requireStudyMember(studyId, memberId);

        int safePage = (page == null || page < 0) ? 0 : page;
        int safeSize = (size == null || size < 1) ? 10 : Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Question> questionPage = inquiryRepository.findByStudyIdAndDeletedAtIsNullOrderByCreatedAtDesc(studyId, pageable);

        return InquiryConverter.toQuestionList(questionPage, !study.getIsActive(), memberId);
    }

    /**
     * 질문 상세 조회. 조회에 성공하면 이 질문과 여기에 달린 답글의 알림을 읽음 처리한다
     * (알림함의 "질문 보기"/"답글 보기"가 모두 이 API로 들어오므로 두 경로가 한 번에 처리된다).
     *
     * <p>읽음 처리는 부가 작업이라 실패해도 조회 응답을 막지 않는다({@link #markReadQuietly}).
     */
    @Transactional(readOnly = true)
    public InquiryResDTO.QuestionDetail getQuestionDetail(Long studyId, Long questionId, Long memberId) {
        Study study = finder.getStudy(studyId);
        finder.requireStudyMember(studyId, memberId);

        Question question = finder.getQuestionInStudy(studyId, questionId);
        List<InquiryResDTO.ImageInfo> images = imageService.getQuestionImages(questionId);

        List<Answer> answers = inquiryReplyRepository.findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(questionId);
        List<Long> answerIds = answers.stream().map(Answer::getId).collect(Collectors.toList());
        Map<Long, List<InquiryResDTO.ImageInfo>> imagesByAnswer = imageService.getAnswerImagesByAnswerIds(answerIds);

        List<InquiryResDTO.AnswerDetail> answerList = answers.stream()
                .map(answer -> InquiryConverter.toAnswerDetail(
                        answer, imagesByAnswer.getOrDefault(answer.getId(), List.of()), memberId))
                .collect(Collectors.toList());

        InquiryResDTO.QuestionDetail detail =
                InquiryConverter.toQuestionDetail(question, images, answerList, !study.getIsActive(), memberId);

        markReadQuietly(questionId, memberId, Boolean.TRUE.equals(detail.isMine()), answers);

        return detail;
    }

    /**
     * 읽음 처리는 조회의 부가 작업이므로 실패해도 응답을 막지 않는다.
     * 쓰기 트랜잭션을 따로 여는 비용(커넥션 추가 점유)이 있어, 실제로 바꿀 게 있을 때만 호출한다.
     * 판단은 이미 로드한 답글 목록과 exists 조회로 현재 readOnly 트랜잭션 안에서 끝낸다.
     */
    private void markReadQuietly(Long questionId, Long memberId, boolean viewerIsAuthor, List<Answer> answers) {
        boolean markAnswers = viewerIsAuthor && answers.stream()
                .anyMatch(answer -> answer.getReadAtByAsker() == null);
        boolean markQuestion =
                !inquiryReadRepository
                        .existsByMemberIdAndQuestionIdAndInquiryStatus(
                                memberId,
                                questionId,
                                InquiryStatus.CHECKED
                        );
        if (!markQuestion && !markAnswers) {
            return;   // 이미 다 읽은 질문 — 쓰기 트랜잭션을 열지 않는다
        }

        try {
            readService.markRead(questionId, memberId, markQuestion, markAnswers);
        } catch (DataIntegrityViolationException e) {
            // 동시 요청이 먼저 읽음 처리함(유니크 제약) — 최종 상태가 같으므로 조용히 넘어간다
        } catch (RuntimeException e) {
            // 락 타임아웃·커넥션 실패 등. 알림 숫자가 잠시 안 줄어들 뿐이라 조회를 실패시키지 않는다
            log.warn("질문 읽음 처리 실패 — questionId={}, memberId={}: {}", questionId, memberId, e.getMessage());
        }
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
            createQuestionReads(
                    studyId,
                    memberId,
                    question
            );
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
        finder.requireQuestionOwner(question, memberId);
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
        finder.requireQuestionOwner(question, memberId);
        finder.requireStudyActive(question.getStudy());

        List<Answer> answers = inquiryReplyRepository.findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(questionId);
        List<Long> answerIds = answers.stream().map(Answer::getId).collect(Collectors.toList());
        imageService.deleteAnswerImagesByAnswerIds(answerIds);   // 답글별 조회 대신 IN 조회 1번
        inquiryReplyRepository.deleteAll(answers);

        imageService.deleteQuestionImages(questionId);
        inquiryReadRepository.deleteByQuestionId(questionId);   // FK 때문에 읽음 기록도 질문보다 먼저
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

    private void createQuestionReads(
            Long studyId,
            Long writerId,
            Question question
    ) {
        List<Member> studyMembers =
                memberStudyRepository.findMembersByStudyId(studyId);

        List<QuestionRead> questionReads = studyMembers.stream()
                .map(member -> QuestionRead.builder()
                        .member(member)
                        .question(question)
                        .inquiryStatus(
                                member.getId().equals(writerId)
                                        ? InquiryStatus.CHECKED
                                        : InquiryStatus.UNCHECKED
                        )
                        .build())
                .toList();

        inquiryReadRepository.saveAll(questionReads);
    }
}
