package com.stology.be.domain.inquiry.repository;

import com.stology.be.domain.study.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InquiryReplyRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long questionId);

    Optional<Answer> findByIdAndQuestionIdAndDeletedAtIsNull(Long id, Long questionId);

    /** 질문과 같은 이유로 soft delete된 행까지 포함해 조회한다. */
    Optional<Answer> findByIdAndQuestionId(Long id, Long questionId);
}
