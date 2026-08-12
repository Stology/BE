package com.stology.be.domain.report.scheduler;

import com.stology.be.domain.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportScheduler {

    private final ReportService reportService;

    // 매 10분마다 실행 - 0 0/10 * * * *
    @Scheduled(cron = "0 0/10 * * * *")
    public void generateWeeklyReports() {
        log.info("Starting weekly report generation scheduler...");
        List<Long> activeStudyIds = reportService.getActiveStudyIds();

        for (Long studyId : activeStudyIds) {
            try {
                reportService.checkAndGenerateMissingReports(studyId);
            } catch (Exception e) {
                log.error("Failed to generate report for study {}: {}", studyId, e.getMessage());
            }
        }
        log.info("Finished weekly report generation scheduler.");
    }
}
