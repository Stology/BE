package com.stology.be.domain.report.repository;

import com.stology.be.domain.report.entity.ReportReadHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportReadHistoryRepository extends JpaRepository<ReportReadHistory, Long> {
    Optional<ReportReadHistory> findByMemberIdAndStudyId(Long memberId, Long studyId);
}
