package com.stology.be.domain.inquiry.repository;

import com.stology.be.domain.study.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Question, Long> {

    Page<Question> findByStudyIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long studyId, Pageable pageable);

    /** hard delete라 삭제된 질문은 행이 없다. 없으면 서비스에서 404(NOT_FOUND)로 처리한다. */
    Optional<Question> findByIdAndStudyId(Long id, Long studyId);
}
