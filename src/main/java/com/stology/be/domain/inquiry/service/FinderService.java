package com.stology.be.domain.inquiry.service;

import com.stology.be.domain.inquiry.exception.InquiryErrorCode;
import com.stology.be.domain.inquiry.exception.InquiryException;
import com.stology.be.domain.inquiry.repository.InquiryReplyRepository;
import com.stology.be.domain.inquiry.repository.InquiryRepository;
import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.exception.MemberException;
import com.stology.be.domain.member.exception.code.MemberErrorCode;
import com.stology.be.domain.member.repository.MemberRepository;
import com.stology.be.domain.study.entity.Answer;
import com.stology.be.domain.study.entity.Question;
import com.stology.be.domain.study.entity.Study;
import com.stology.be.domain.study.exception.StudyException;
import com.stology.be.domain.study.exception.code.StudyErrorCode;
import com.stology.be.domain.study.repository.MemberStudyRepository;
import com.stology.be.domain.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * inquiry 도메인 공용: 엔티티 조회와 권한/상태 검증을 모아둔다.
 * 질문/답글 서비스가 공통으로 쓰는 "찾고 검증하는" 로직만 담당한다.
 */
@Service
@RequiredArgsConstructor
public class FinderService {

    private final StudyRepository studyRepository;
    private final MemberRepository memberRepository;
    private final MemberStudyRepository memberStudyRepository;
    private final InquiryRepository inquiryRepository;
    private final InquiryReplyRepository inquiryReplyRepository;

    public Study getStudy(Long studyId) {
        return studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyException(StudyErrorCode.STUDY_NOT_FOUND));
    }

    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    public void requireStudyMember(Long studyId, Long memberId) {
        if (!memberStudyRepository.existsByStudyIdAndMemberId(studyId, memberId)) {
            throw new InquiryException(InquiryErrorCode.STUDY_MEMBER_FORBIDDEN);
        }
    }

    public void requireStudyActive(Study study) {
        if (!study.getIsActive()) {
            throw new InquiryException(InquiryErrorCode.STUDY_ENDED);
        }
    }

    public Study getActiveStudyForMember(Long studyId, Long memberId) {
        Study study = getStudy(studyId);
        requireStudyMember(studyId, memberId);
        requireStudyActive(study);
        return study;
    }

    /** hard delete라 삭제된 글은 행이 사라지므로, 없으면 404(NOT_FOUND)로 처리한다. */
    public Question getQuestionInStudy(Long studyId, Long questionId) {
        return inquiryRepository.findByIdAndStudyId(questionId, studyId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));
    }

    public Answer getAnswerInQuestion(Long studyId, Long questionId, Long answerId) {
        getQuestionInStudy(studyId, questionId);

        return inquiryReplyRepository.findByIdAndQuestionId(answerId, questionId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.REPLY_NOT_FOUND));
    }

    /**
     * ERD상 question/answer는 작성자를 member FK가 아닌 memberName 문자열로만 저장하므로,
     * 본인 여부는 로그인한 회원의 이름과 memberName 문자열 일치로 판별한다(동명이인 시 오탐 가능).
     */
    public void requireQuestionOwner(Question question, Member member) {
        if (!question.getMemberName().equals(member.getName())) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_FORBIDDEN);
        }
    }

    public void requireAnswerOwner(Answer answer, Member member) {
        if (!answer.getMemberName().equals(member.getName())) {
            throw new InquiryException(InquiryErrorCode.REPLY_FORBIDDEN);
        }
    }
}
