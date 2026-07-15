package com.stology.be.domain.inquiry.repository;

import com.stology.be.domain.study.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Question, Long> {

    Page<Question> findByStudyIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long studyId, Pageable pageable);

    Optional<Question> findByIdAndStudyIdAndDeletedAtIsNull(Long id, Long studyId);
}
