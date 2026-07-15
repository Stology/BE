package com.stology.be.domain.inquiry.repository;

import com.stology.be.domain.study.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InquiryReplyRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long questionId);

    Optional<Answer> findByIdAndQuestionIdAndDeletedAtIsNull(Long id, Long questionId);
}
