package com.stology.be.domain.inquiry.repository;

import com.stology.be.domain.inquiry.enums.InquiryStatus;
import com.stology.be.domain.study.entity.QuestionRead;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
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

    @Query("""
    SELECT qr.question.id
    FROM QuestionRead qr
    WHERE qr.member.id = :memberId
      AND qr.question.deletedAt IS NULL
      AND (
          qr.inquiryStatus = :unchecked
          OR (
              qr.inquiryStatus = :checked
              AND qr.updatedAt >= :startOfDay
              AND qr.updatedAt < :endOfDay
          )
      )
""")
    //홈화면에서 띄울 질문(미확인 or 확인 and 당일 전) 갯수 찾기
    List<Long> findTodoQuestionIds(
            @Param("memberId") Long memberId,
            @Param("unchecked") InquiryStatus unchecked,
            @Param("checked") InquiryStatus checked,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
    SELECT qr
    FROM QuestionRead qr
    JOIN FETCH qr.question q
    JOIN FETCH q.study s
    WHERE qr.member.id = :memberId
      AND q.deletedAt IS NULL
      AND (
          qr.inquiryStatus =
              com.stology.be.domain.inquiry.enums.InquiryStatus.UNCHECKED
          OR (
              qr.inquiryStatus =
                  com.stology.be.domain.inquiry.enums.InquiryStatus.CHECKED
              AND qr.updatedAt >= :startOfDay
              AND qr.updatedAt < :endOfDay
          )
      )
      AND (
          :cursorTime IS NULL
          OR q.createdAt < :cursorTime
          OR (
              q.createdAt = :cursorTime
              AND (
                  1 < :cursorTypeRank
                  OR (
                      1 = :cursorTypeRank
                      AND q.id < :cursorId
                  )
              )
          )
      )
    ORDER BY q.createdAt DESC, q.id DESC
""")
    Slice<QuestionRead> findQuestionActivities(
            @Param("memberId") Long memberId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorTypeRank") Integer cursorTypeRank,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

}
