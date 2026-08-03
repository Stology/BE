package com.stology.be.domain.inquiry.repository;

import com.stology.be.domain.inquiry.enums.InquiryStatus;
import com.stology.be.domain.study.entity.QuestionRead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InquiryReadRepository extends JpaRepository<QuestionRead, Long> {

    /**
     * 질문 hard delete 시 FK 때문에 읽음 기록을 먼저 지운다.
     * 한 질문의 읽음 행 수는 스터디원 수 이하라 파생 delete로도 충분하다.
     */
    void deleteByQuestionId(Long questionId);


    //한 사람의 질문의 읽음 여부 테이블 가져오기
    Optional<QuestionRead> findByMemberIdAndQuestionId(
            Long memberId,
            Long questionId
    );

    // 질문 읽음 테이블에서 특정 상태가 존재하는지 찾아보기.
    boolean existsByMemberIdAndQuestionIdAndInquiryStatus(
            Long memberId,
            Long questionId,
            InquiryStatus inquiryStatus
    );
}
