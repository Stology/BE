package com.stology.be.domain.inquiry.repository;

import com.stology.be.domain.study.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InquiryReplyRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long questionId);

    /** hard delete라 삭제된 답글은 행이 없다. 없으면 서비스에서 404(NOT_FOUND)로 처리한다. */
    Optional<Answer> findByIdAndQuestionId(Long id, Long questionId);

    /**
     * 질문 작성자가 질문을 열었을 때 그 질문의 답글을 한 번에 읽음 처리한다.
     * 엔티티를 로드해 수정하면 @LastModifiedDate가 updated_at을 갱신해 "수정된 답글"로 오인되므로
     * 벌크 UPDATE로 읽음 컬럼만 건드린다(감사 필드는 그대로 유지된다).
     *
     * <p>이미 읽은 답글은 시각을 덮어쓰지 않는다(IS NULL 조건) — 다시 열었다고 읽은 시각이 밀리면
     * 알림함의 "자정 이후 제거"가 계속 연장된다.
     */
    @Modifying
    @Query("update Answer a set a.readAtByAsker = CURRENT_TIMESTAMP "
            + "where a.question.id = :questionId and a.readAtByAsker is null and a.deletedAt is null")
    int markAllReadByAsker(@Param("questionId") Long questionId);
}
