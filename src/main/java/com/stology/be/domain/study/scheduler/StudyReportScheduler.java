package com.stology.be.domain.study.scheduler;

import com.stology.be.domain.report.service.ReportService;
import com.stology.be.domain.study.entity.Study;
import com.stology.be.domain.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyReportScheduler {

    private final StudyRepository studyRepository;
    private final ReportService reportService;

    // 매 1시간마다 실행 (정각) - 0 0 * * * *
    @Scheduled(cron = "0 0 * * * *")
    public void generateWeeklyReports() {
        log.info("Starting weekly report generation scheduler...");
        List<Study> activeStudies = studyRepository.findByIsActiveTrue();

        for (Study study : activeStudies) {
            try {
                reportService.checkAndGenerateMissingReports(study.getId());
            } catch (Exception e) {
                log.error("Failed to check or generate report for study {}: {}", study.getId(), e.getMessage());
            }
        }
        log.info("Finished weekly report generation scheduler.");
    }
}
