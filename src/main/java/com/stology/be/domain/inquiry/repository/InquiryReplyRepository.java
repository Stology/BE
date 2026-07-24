package com.stology.be.domain.inquiry.repository;

import com.stology.be.domain.study.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InquiryReplyRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long questionId);

    /** hard delete라 삭제된 답글은 행이 없다. 없으면 서비스에서 404(NOT_FOUND)로 처리한다. */
    Optional<Answer> findByIdAndQuestionId(Long id, Long questionId);
}
