package com.stology.be.domain.inquiry.service;

import com.stology.be.domain.inquiry.converter.InquiryConverter;
import com.stology.be.domain.inquiry.dto.request.InquiryReqDTO;
import com.stology.be.domain.inquiry.dto.response.InquiryResDTO;
import com.stology.be.domain.inquiry.exception.InquiryErrorCode;
import com.stology.be.domain.inquiry.exception.InquiryException;
import com.stology.be.domain.inquiry.repository.InquiryImageRepository;
import com.stology.be.domain.inquiry.repository.InquiryReplyImageRepository;
import com.stology.be.domain.inquiry.repository.InquiryReplyRepository;
import com.stology.be.domain.inquiry.repository.InquiryRepository;
import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.exception.MemberException;
import com.stology.be.domain.member.exception.code.MemberErrorCode;
import com.stology.be.domain.member.repository.MemberRepository;
import com.stology.be.domain.study.entity.Answer;
import com.stology.be.domain.study.entity.AnswerImage;
import com.stology.be.domain.study.entity.Question;
import com.stology.be.domain.study.entity.QuestionImage;
import com.stology.be.domain.study.entity.Study;
import com.stology.be.domain.study.exception.StudyException;
import com.stology.be.domain.study.exception.code.StudyErrorCode;
import com.stology.be.domain.study.repository.MemberStudyRepository;
import com.stology.be.domain.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryReplyRepository inquiryReplyRepository;
    private final InquiryImageRepository inquiryImageRepository;
    private final InquiryReplyImageRepository inquiryReplyImageRepository;
    private final StudyRepository studyRepository;
    private final MemberStudyRepository memberStudyRepository;
    private final MemberRepository memberRepository;
    private final InquiryImageUploader inquiryImageUploader;

    /** 본문에 심긴 이미지 자리표시자. 프론트의 [[img:N]] 토큰과 같은 형식이다. */
    private static final Pattern IMAGE_TOKEN = Pattern.compile("\\[\\[img:\\d+]]");

    @Override
    public InquiryResDTO.QuestionList getQuestions(Long studyId, Integer page, Integer size, Long memberId) {
        Study study = getStudy(studyId);
        requireStudyMember(studyId, memberId);
        Member member = getMember(memberId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Question> questionPage = inquiryRepository.findByStudyIdAndDeletedAtIsNullOrderByCreatedAtDesc(studyId, pageable);

        return InquiryConverter.toQuestionList(questionPage, !study.getIsActive(), member.getName());
    }

    @Override
    public InquiryResDTO.QuestionDetail getQuestionDetail(Long studyId, Long questionId, Long memberId) {
        Study study = getStudy(studyId);
        requireStudyMember(studyId, memberId);
        Member member = getMember(memberId);

        Question question = getQuestionInStudy(studyId, questionId);
        List<InquiryResDTO.ImageInfo> images = getQuestionImages(questionId);

        List<Answer> answers = inquiryReplyRepository.findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(questionId);
        List<InquiryResDTO.AnswerDetail> answerList = answers.stream()
                .map(answer -> InquiryConverter.toAnswerDetail(answer, getAnswerImages(answer.getId()), member.getName()))
                .collect(Collectors.toList());

        return InquiryConverter.toQuestionDetail(question, images, answerList, !study.getIsActive(), member.getName());
    }

    @Override
    @Transactional
    public InquiryResDTO.WriteQuestionResult writeQuestion(Long studyId, Long memberId, InquiryReqDTO.WriteQuestion request) {
        validateTitle(request.title());
        validateContent(request.content());

        Study study = getActiveStudyForMember(studyId, memberId);
        Member member = getMember(memberId);

        Question question = InquiryConverter.toQuestion(request, study, member);
        inquiryRepository.save(question);
        attachQuestionImages(question, request.imageUrls());

        return new InquiryResDTO.WriteQuestionResult(question.getId());
    }

    @Override
    @Transactional
    public InquiryResDTO.UpdateQuestionResult updateQuestion(Long studyId, Long questionId, Long memberId, InquiryReqDTO.UpdateQuestion request) {
        validateTitle(request.title());
        validateContent(request.content());

        Question question = getQuestionInStudy(studyId, questionId);
        Member member = getMember(memberId);
        requireQuestionOwner(question, member);
        requireStudyActive(question.getStudy());

        question.update(request.title(), request.content());
        replaceQuestionImages(question, request.imageUrls());

        return new InquiryResDTO.UpdateQuestionResult(question.getId());
    }

    @Override
    @Transactional
    public void deleteQuestion(Long studyId, Long questionId, Long memberId) {
        Question question = getQuestionInStudy(studyId, questionId);
        Member member = getMember(memberId);
        requireQuestionOwner(question, member);
        requireStudyActive(question.getStudy());

        List<Answer> answers = inquiryReplyRepository.findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(questionId);
        for (Answer answer : answers) {
            inquiryReplyImageRepository.findByAnswerIdAndDeletedAtIsNullOrderByIdAsc(answer.getId())
                    .forEach(AnswerImage::delete);
            answer.delete();
        }

        inquiryImageRepository.findByQuestionIdAndDeletedAtIsNullOrderByIdAsc(questionId)
                .forEach(QuestionImage::delete);

        question.delete();
    }

    @Override
    @Transactional
    public InquiryResDTO.UploadImageResult uploadQuestionImages(Long studyId, Long questionId, Long memberId, List<MultipartFile> files) {
        Question question = getQuestionInStudy(studyId, questionId);
        Member member = getMember(memberId);
        requireQuestionOwner(question, member);
        requireStudyActive(question.getStudy());

        List<String> urls = inquiryImageUploader.uploadAll(files, "question/" + questionId);
        List<QuestionImage> images = urls.stream()
                .map(url -> InquiryConverter.toQuestionImage(url, question))
                .collect(Collectors.toList());
        inquiryImageRepository.saveAll(images);

        question.updateAttached(true);

        List<InquiryResDTO.UploadedImage> result = images.stream()
                .map(image -> new InquiryResDTO.UploadedImage(
                        image.getId(),
                        image.getImageUrl(),
                        inquiryImageUploader.toDisplayUrl(image.getImageUrl())))
                .collect(Collectors.toList());
        return new InquiryResDTO.UploadImageResult(result);
    }

    @Override
    @Transactional
    public InquiryResDTO.WriteAnswerResult writeAnswer(Long studyId, Long questionId, Long memberId, InquiryReqDTO.WriteAnswer request) {
        validateReplyContent(request.content());

        Question question = getQuestionInStudy(studyId, questionId);
        requireStudyMember(studyId, memberId);
        requireStudyActive(question.getStudy());
        Member member = getMember(memberId);

        Answer answer = InquiryConverter.toAnswer(request, question, member);
        inquiryReplyRepository.save(answer);
        attachAnswerImages(answer, request.imageUrls());
        question.increaseAnswerCount();

        return new InquiryResDTO.WriteAnswerResult(answer.getId());
    }

    @Override
    @Transactional
    public InquiryResDTO.UpdateAnswerResult updateAnswer(Long studyId, Long questionId, Long answerId, Long memberId, InquiryReqDTO.UpdateAnswer request) {
        validateReplyContent(request.content());

        Answer answer = getAnswerInQuestion(studyId, questionId, answerId);
        Member member = getMember(memberId);
        requireAnswerOwner(answer, member);
        requireStudyActive(answer.getQuestion().getStudy());

        answer.updateContent(request.content());
        replaceAnswerImages(answer, request.imageUrls());

        return new InquiryResDTO.UpdateAnswerResult(answer.getId());
    }

    @Override
    @Transactional
    public void deleteAnswer(Long studyId, Long questionId, Long answerId, Long memberId) {
        Answer answer = getAnswerInQuestion(studyId, questionId, answerId);
        Member member = getMember(memberId);
        requireAnswerOwner(answer, member);
        requireStudyActive(answer.getQuestion().getStudy());

        inquiryReplyImageRepository.findByAnswerIdAndDeletedAtIsNullOrderByIdAsc(answerId)
                .forEach(AnswerImage::delete);
        answer.delete();
        answer.getQuestion().decreaseAnswerCount();
    }

    @Override
    @Transactional
    public InquiryResDTO.UploadImageResult uploadAnswerImages(Long studyId, Long questionId, Long answerId, Long memberId, List<MultipartFile> files) {
        Answer answer = getAnswerInQuestion(studyId, questionId, answerId);
        Member member = getMember(memberId);
        requireAnswerOwner(answer, member);
        requireStudyActive(answer.getQuestion().getStudy());

        List<String> urls = inquiryImageUploader.uploadAll(files, "answer/" + answerId);
        List<AnswerImage> images = urls.stream()
                .map(url -> InquiryConverter.toAnswerImage(url, answer))
                .collect(Collectors.toList());
        inquiryReplyImageRepository.saveAll(images);

        List<InquiryResDTO.UploadedImage> result = images.stream()
                .map(image -> new InquiryResDTO.UploadedImage(
                        image.getId(),
                        image.getImageUrl(),
                        inquiryImageUploader.toDisplayUrl(image.getImageUrl())))
                .collect(Collectors.toList());
        return new InquiryResDTO.UploadImageResult(result);
    }

    /**
     * 질문 작성 모달처럼 아직 questionId가 없는 시점에 이미지를 먼저 올린다.
     * S3에만 올리고 DB에는 남기지 않으므로, 모달을 취소해도 고아 레코드가 생기지 않는다.
     * 반환된 imageUrl을 질문 작성/수정 요청 body의 imageUrls에 담아 보내면 그때 연결된다.
     */
    @Override
    public InquiryResDTO.StageImageResult stageQuestionImages(Long studyId, Long memberId, List<MultipartFile> files) {
        getActiveStudyForMember(studyId, memberId);
        return stage(files, "question/staging");
    }

    /** 답글 작성 시점에도 answerId가 없으므로 동일하게 선업로드를 제공한다. */
    @Override
    public InquiryResDTO.StageImageResult stageAnswerImages(Long studyId, Long questionId, Long memberId, List<MultipartFile> files) {
        getActiveStudyForMember(studyId, memberId);
        getQuestionInStudy(studyId, questionId);
        return stage(files, "answer/staging");
    }

    private InquiryResDTO.StageImageResult stage(List<MultipartFile> files, String subDirectory) {
        List<InquiryResDTO.StagedImage> images = inquiryImageUploader.uploadAll(files, subDirectory).stream()
                .map(url -> new InquiryResDTO.StagedImage(url, inquiryImageUploader.toDisplayUrl(url)))
                .collect(Collectors.toList());
        return new InquiryResDTO.StageImageResult(images);
    }

    private void attachQuestionImages(Question question, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        List<QuestionImage> images = imageUrls.stream()
                .map(inquiryImageUploader::toStoredUrl)
                .map(url -> InquiryConverter.toQuestionImage(url, question))
                .collect(Collectors.toList());
        inquiryImageRepository.saveAll(images);
        question.updateAttached(true);
    }

    /** imageUrls가 null이면 이미지를 건드리지 않고, 그 외에는 전달된 목록으로 전체 교체한다. */
    private void replaceQuestionImages(Question question, List<String> imageUrls) {
        if (imageUrls == null) {
            return;
        }
        inquiryImageRepository.findByQuestionIdAndDeletedAtIsNullOrderByIdAsc(question.getId())
                .forEach(QuestionImage::delete);
        question.updateAttached(false);
        attachQuestionImages(question, imageUrls);
    }

    private void attachAnswerImages(Answer answer, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        List<AnswerImage> images = imageUrls.stream()
                .map(inquiryImageUploader::toStoredUrl)
                .map(url -> InquiryConverter.toAnswerImage(url, answer))
                .collect(Collectors.toList());
        inquiryReplyImageRepository.saveAll(images);
    }

    private void replaceAnswerImages(Answer answer, List<String> imageUrls) {
        if (imageUrls == null) {
            return;
        }
        inquiryReplyImageRepository.findByAnswerIdAndDeletedAtIsNullOrderByIdAsc(answer.getId())
                .forEach(AnswerImage::delete);
        attachAnswerImages(answer, imageUrls);
    }

    private List<InquiryResDTO.ImageInfo> getQuestionImages(Long questionId) {
        return inquiryImageRepository.findByQuestionIdAndDeletedAtIsNullOrderByIdAsc(questionId).stream()
                .map(image -> new InquiryResDTO.ImageInfo(
                        image.getId(),
                        image.getImageUrl(),
                        inquiryImageUploader.toDisplayUrl(image.getImageUrl())))
                .collect(Collectors.toList());
    }

    private List<InquiryResDTO.ImageInfo> getAnswerImages(Long answerId) {
        return inquiryReplyImageRepository.findByAnswerIdAndDeletedAtIsNullOrderByIdAsc(answerId).stream()
                .map(image -> new InquiryResDTO.ImageInfo(
                        image.getId(),
                        image.getImageUrl(),
                        inquiryImageUploader.toDisplayUrl(image.getImageUrl())))
                .collect(Collectors.toList());
    }

    private Study getStudy(Long studyId) {
        return studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyException(StudyErrorCode.STUDY_NOT_FOUND));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private void requireStudyMember(Long studyId, Long memberId) {
        if (!memberStudyRepository.existsByStudyIdAndMemberId(studyId, memberId)) {
            throw new InquiryException(InquiryErrorCode.STUDY_MEMBER_FORBIDDEN);
        }
    }

    private void requireStudyActive(Study study) {
        if (!study.getIsActive()) {
            throw new InquiryException(InquiryErrorCode.STUDY_ENDED);
        }
    }

    private Study getActiveStudyForMember(Long studyId, Long memberId) {
        Study study = getStudy(studyId);
        requireStudyMember(studyId, memberId);
        requireStudyActive(study);
        return study;
    }

    private Question getQuestionInStudy(Long studyId, Long questionId) {
        return inquiryRepository.findByIdAndStudyIdAndDeletedAtIsNull(questionId, studyId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));
    }

    private Answer getAnswerInQuestion(Long studyId, Long questionId, Long answerId) {
        getQuestionInStudy(studyId, questionId);
        return inquiryReplyRepository.findByIdAndQuestionIdAndDeletedAtIsNull(answerId, questionId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.REPLY_NOT_FOUND));
    }

    /**
     * ERD상 question/answer는 작성자를 member FK가 아닌 memberName 문자열로만 저장하므로,
     * 본인 여부는 로그인한 회원의 이름과 memberName 문자열 일치로 판별한다(동명이인 시 오탐 가능).
     */
    private void requireQuestionOwner(Question question, Member member) {
        if (!question.getMemberName().equals(member.getName())) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_FORBIDDEN);
        }
    }

    private void requireAnswerOwner(Answer answer, Member member) {
        if (!answer.getMemberName().equals(member.getName())) {
            throw new InquiryException(InquiryErrorCode.REPLY_FORBIDDEN);
        }
    }

    /** 이미지 자리표시자를 제외한 실제 텍스트 길이 */
    private int textLength(String content) {
        return IMAGE_TOKEN.matcher(content).replaceAll("").length();
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
        if (content == null || content.isBlank() || textLength(content) > 1000) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_BODY_INVALID);
        }
    }

    private void validateReplyContent(String content) {
        if (content == null || content.isBlank()) {
            throw new InquiryException(InquiryErrorCode.REPLY_BODY_INVALID);
        }
    }
}
