package com.stology.be.domain.inquiry.repository;

import com.stology.be.domain.study.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Question, Long> {

    Page<Question> findByStudyIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long studyId, Pageable pageable);

    Optional<Question> findByIdAndStudyIdAndDeletedAtIsNull(Long id, Long studyId);

    /**
     * soft delete된 행까지 포함해 조회한다.
     * "삭제됨(410)"과 "존재하지 않음(404)"을 구분하려면 삭제 여부를 서비스에서 판정해야 한다.
     */
    Optional<Question> findByIdAndStudyId(Long id, Long studyId);
}
