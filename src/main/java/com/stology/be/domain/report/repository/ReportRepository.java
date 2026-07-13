package com.stology.be.domain.report.repository;

import com.stology.be.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findAllByStudyIdOrderByCreatedAtAsc(Long studyId);
    Optional<Report> findByIdAndStudyId(Long id, Long studyId);
}
