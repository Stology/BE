package com.stology.be.domain.report.service;

import com.stology.be.domain.node.repository.StudyNodeRepository;
import com.stology.be.domain.report.dto.response.ReportResponseDto.*;
import com.stology.be.domain.report.entity.Report;
import com.stology.be.domain.report.repository.ReportRepository;
import com.stology.be.domain.study.repository.QuestionRepository;
import com.stology.be.global.apiPayload.code.GeneralErrorCode;
import com.stology.be.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional // 조회 시 데이터 갱신(INSERT)이 발생할 수 있으므로 readOnly=true 해제
public class ReportService {

    private final ReportRepository reportRepository;
    private final QuestionRepository questionRepository;
    private final StudyNodeRepository studyNodeRepository;
    private final GeminiApiService geminiApiService;

    // 온디맨드 갱신 처리 (데이터 변경이 감지되면 새 리포트 생성 및 저장)
    protected Report checkAndUpdateReport(Long studyId, Report currentReport) {
        LocalDateTime lastReportTime = (currentReport != null) ? currentReport.getCreatedAt() : LocalDateTime.MIN;
        
        // 1. Question과 StudyNode의 최신 업데이트 시간(updatedAt) 조회
        LocalDateTime latestQuestionTime = questionRepository.findTopByStudyIdOrderByUpdatedAtDesc(studyId)
                .map(q -> q.getUpdatedAt())
                .orElse(LocalDateTime.MIN);
                
        LocalDateTime latestNodeTime = studyNodeRepository.findTopByStudyIdOrderByUpdatedAtDesc(studyId)
                .map(n -> n.getUpdatedAt())
                .orElse(LocalDateTime.MIN);
                
        // 2. 둘 중 더 최근에 변경된 시간을 추출
        LocalDateTime latestActivityTime = latestQuestionTime.isAfter(latestNodeTime) ? latestQuestionTime : latestNodeTime;

        // 3. 최근 변경 시간이 기존 리포트 생성 시간보다 나중이라면 갱신 시작
        if (latestActivityTime.isAfter(lastReportTime)) {
            log.info("새로운 수정/생성 활동이 감지되었습니다. (최신 활동: {}, 기존 리포트 생성: {}) Gemini 갱신을 시작합니다.", latestActivityTime, lastReportTime);
            
            if(currentReport == null) {
               throw new GeneralException(GeneralErrorCode.NOT_FOUND); // 첫 리포트가 없는 경우는 다른 플로우(ex: 첫 가입)에서 처리해야 함
            }
            
            Report newReport = geminiApiService.generateNewReport(currentReport.getStudy());
            return reportRepository.save(newReport);
        }
        
        return currentReport;
    }

    public Report getReportByWeek(Long studyId, Integer week) {
        List<Report> reports = reportRepository.findAllByStudyIdOrderByCreatedAtAsc(studyId);
        
        if (reports.isEmpty()) {
            throw new GeneralException(GeneralErrorCode.NOT_FOUND);
        }
        
        Report targetReport;
        if (week == null) {
            targetReport = reports.get(reports.size() - 1);
            // 최신 리포트 요청(week == null)인 경우에만 온디맨드 갱신 검사 수행
            targetReport = checkAndUpdateReport(studyId, targetReport);
        } else {
            if (week < 1 || week > reports.size()) {
                throw new GeneralException(GeneralErrorCode.NOT_FOUND);
            }
            targetReport = reports.get(week - 1);
        }
        
        return targetReport;
    }

    public ReportSummaryResponse getReportSummary(Long studyId, Integer week) {
        Report report = getReportByWeek(studyId, week);
        
        int totalNewAndReinforced = report.getNewActiveNodeCount() + report.getReinforcedNodeCount();
        int newPercentage = 0;
        int reinforcedPercentage = 0;
        
        if (totalNewAndReinforced > 0) {
            newPercentage = (int) Math.round((double) report.getNewActiveNodeCount() / totalNewAndReinforced * 100);
            reinforcedPercentage = 100 - newPercentage;
        }
        
        return ReportSummaryResponse.builder()
                .reportId(report.getId())
                .totalNodeCount(report.getTotalNodeCount())
                .newActiveNodeCount(report.getNewActiveNodeCount())
                .newActiveNodePercentage(newPercentage)
                .reinforcedNodeCount(report.getReinforcedNodeCount())
                .reinforcedNodePercentage(reinforcedPercentage)
                .build();
    }

    public WeeklyCoreNodeResponse getWeeklyCoreNode(Long studyId, Integer week) {
        Report report = getReportByWeek(studyId, week);
        return WeeklyCoreNodeResponse.builder()
                .weeklyCoreNodeList(report.getWeeklyCoreNodeList())
                .build();
    }

    public AiReviewResponse getAiReview(Long studyId, Long aiReviewId) {
        // AI 리뷰는 특정 reportId(aiReviewId)를 콕 집어 요청하므로 갱신 검사 불필요
        Report report = reportRepository.findByIdAndStudyId(aiReviewId, studyId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND));
                
        return AiReviewResponse.builder()
                .aiReviewContent(report.getAiReviewContent())
                .build();
    }

    public RecommendedNodeResponse getRecommendedNode(Long studyId, Integer week) {
        Report report = getReportByWeek(studyId, week);
        return RecommendedNodeResponse.builder()
                .recommendedNodeList(report.getRecommendedNodeList())
                .build();
    }

    public FollowUpResponse getFollowUp(Long studyId, Integer week) {
        Report report = getReportByWeek(studyId, week);
        return FollowUpResponse.builder()
                .followUpContent(report.getFollowUpContent())
                .build();
    }

    public MemberActivityStatisticsResponse getStatistics(Long studyId, Integer week) {
        Report report = getReportByWeek(studyId, week);
        return MemberActivityStatisticsResponse.builder()
                .memberActivityStatisticsList(report.getMemberActivityStatisticsList())
                .build();
    }
}
