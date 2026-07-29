package com.stology.be.domain.inquiry.repository;

import com.stology.be.domain.study.entity.QuestionRead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryReadRepository extends JpaRepository<QuestionRead, Long> {

    /** 이미 읽은 질문이면 행을 또 만들지 않는다(유니크 제약은 동시 요청 대비 최후 방어선). */
    boolean existsByMemberIdAndQuestionId(Long memberId, Long questionId);

    /**
     * 질문 hard delete 시 FK 때문에 읽음 기록을 먼저 지운다.
     * 한 질문의 읽음 행 수는 스터디원 수 이하라 파생 delete로도 충분하다.
     */
    void deleteByQuestionId(Long questionId);
}
