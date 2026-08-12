package com.stology.be.domain.report.service;

import com.stology.be.domain.node.repository.StudyNodeRepository;
import com.stology.be.domain.report.dto.response.ReportResponseDto.*;
import com.stology.be.global.external.ai.AiReportService;
import com.stology.be.domain.report.entity.Report;
import com.stology.be.domain.report.repository.ReportRepository;

import com.stology.be.domain.report.exception.ReportErrorCode;
import com.stology.be.global.apiPayload.code.GeneralErrorCode;
import com.stology.be.domain.report.exception.ReportException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stology.be.domain.node.entity.StudyNode;
import com.stology.be.domain.node.entity.StudyMaterial;
import com.stology.be.domain.study.entity.Question;
import com.stology.be.domain.study.entity.Study;
import com.stology.be.global.external.ai.dto.AiReportOutputDto;

import com.stology.be.domain.report.dto.WeeklyCoreNodeDto;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final StudyNodeRepository studyNodeRepository;
    private final com.stology.be.domain.study.repository.StudyRepository studyRepository;
    private final AiReportService aiReportService;
    private final com.stology.be.domain.report.repository.ReportReadHistoryRepository reportReadHistoryRepository;
    private final com.stology.be.domain.node.repository.StudyMaterialRepository studyMaterialRepository;
    private final com.stology.be.domain.study.repository.QuestionRepository questionRepository;
    private final com.stology.be.domain.study.repository.MemberStudyRepository memberStudyRepository;

    public Report getReportByWeek(Long studyId, Integer week) {
        List<Report> reports = reportRepository.findAllByStudyIdOrderByCreatedAtAsc(studyId);

        
        if (reports.isEmpty()) {
            throw new ReportException(ReportErrorCode.REPORT_NOT_FOUND);
        }
        
        if (week == null) {
            return reports.get(reports.size() - 1);
        } else {
            if (week < 1 || week > reports.size()) {
                throw new ReportException(ReportErrorCode.REPORT_NOT_FOUND);
            }
            return reports.get(week - 1);
        }
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
        Report report = reportRepository.findByIdAndStudyId(aiReviewId, studyId)
                .orElseThrow(() -> new ReportException(ReportErrorCode.REPORT_NOT_FOUND));
                
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

    public MemberActivityStatisticsResponse getStatistics(Long studyId, Integer week) {
        Report report = getReportByWeek(studyId, week);
        return MemberActivityStatisticsResponse.builder()
                .memberActivityStatisticsList(report.getMemberActivityStatisticsList())
                .build();
    }

    @Transactional
    public FullReportResponse getFullReport(Long studyId, Integer week, Long memberId) {
        List<Report> reports = reportRepository.findAllByStudyIdOrderByCreatedAtAsc(studyId);

        if (reports.isEmpty()) {
            throw new ReportException(ReportErrorCode.REPORT_NOT_FOUND);
        }

        int resolvedWeek;
        Report report;
        if (week == null) {
            resolvedWeek = reports.size();
            report = reports.get(reports.size() - 1);
        } else {
            if (week < 1 || week > reports.size()) {
                throw new ReportException(ReportErrorCode.REPORT_NOT_FOUND);
            }
            resolvedWeek = week;
            report = reports.get(week - 1);
        }

        // 리포트를 조회할 때 자동으로 읽음 처리
        markReportAsRead(studyId, report.getId(), memberId);

        int totalNewAndReinforced = report.getNewActiveNodeCount() + report.getReinforcedNodeCount();
        int newPercentage = 0;
        int reinforcedPercentage = 0;

        if (totalNewAndReinforced > 0) {
            newPercentage = (int) Math.round((double) report.getNewActiveNodeCount() / totalNewAndReinforced * 100);
            reinforcedPercentage = 100 - newPercentage;
        }

        return FullReportResponse.builder()
                .reportId(report.getId())
                .totalWeeks(reports.size())   // 전체 생성된 주차 수 (탭 렌더링용)
                .currentWeek(resolvedWeek)    // 현재 조회 중인 주차
                .totalNodeCount(report.getTotalNodeCount())
                .newActiveNodeCount(report.getNewActiveNodeCount())
                .newActiveNodePercentage(newPercentage)
                .reinforcedNodeCount(report.getReinforcedNodeCount())
                .reinforcedNodePercentage(reinforcedPercentage)
                .weeklyCoreNodeList(report.getWeeklyCoreNodeList())
                .aiReviewContent(report.getAiReviewContent())
                .recommendedNodeList(report.getRecommendedNodeList())
                .memberActivityStatisticsList(report.getMemberActivityStatisticsList())
                .build();
    }

    @Transactional
    public void deleteReport(Long reportId) {
        reportRepository.deleteById(reportId);
    }

    @Transactional
    public void checkAndGenerateMissingReports(Long studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new ReportException(ReportErrorCode.STUDY_NOT_FOUND));

        if (study.getStartDate() == null || study.getStartDate().isAfter(LocalDateTime.now())) return;
        
        int currentWeek = (int) ChronoUnit.WEEKS.between(study.getStartDate(), LocalDateTime.now()) + 1;
        List<Report> reports = reportRepository.findAllByStudyIdOrderByCreatedAtAsc(studyId);
        
        int missingCount = currentWeek - reports.size();
        
        for (int i = 0; i < missingCount; i++) {
            int targetWeek = reports.size() + 1;

            LocalDateTime startOfWeek = study.getStartDate().plusWeeks(targetWeek - 1);
            LocalDateTime endOfWeek = startOfWeek.plusDays(7);

            AiReportOutputDto output = aiReportService.generateNewReport(study, generateDbStatsContent(study, studyId, startOfWeek, endOfWeek));
            
            List<StudyNode> weekNodes = studyNodeRepository.findByStudy_IdAndRecommendWeek(studyId, targetWeek);
            List<StudyNode> activeNodesThisWeek = studyNodeRepository.findActiveNodesBetween(studyId, startOfWeek, endOfWeek);

            List<StudyNode> newNodes = activeNodesThisWeek.stream()
                    .filter(n -> n.getActivationWeek() == targetWeek)
                    .toList();

            List<StudyNode> reinforcedNodes = activeNodesThisWeek.stream()
                    .filter(n -> n.getActivationWeek() > 0 && n.getActivationWeek() < targetWeek)
                    .toList();

            List<WeeklyCoreNodeDto> coreNodeDtoList = new ArrayList<>();
            for (StudyNode node : weekNodes) {
                String state;
                if (node.getActivationWeek() == targetWeek) {
                    state = "신규 활성화";
                } else if (node.getActivationWeek() > 0) {
                    state = "활성";
                } else {
                    state = "비활성";
                }
                coreNodeDtoList.add(new WeeklyCoreNodeDto(node.getTitle(), state, node.getActiveLevel()));
            }

            Report newReport = Report.builder()
                    .study(study)
                    .totalNodeCount(studyNodeRepository.countByStudy_Id(studyId).intValue())
                    .newActiveNodeCount(newNodes.size())
                    .reinforcedNodeCount(reinforcedNodes.size())
                    .weeklyCoreNodeList(coreNodeDtoList)
                    .aiReviewContent(output.getAiReviewContent())
                    .recommendedNodeList(output.getRecommendedNodeList())
                    .memberActivityStatisticsList(output.getMemberActivityStatisticsList())
                    .build();
            reportRepository.save(newReport);
            reports.add(newReport);
            log.info("스터디 {} 의 {}주차 누락된 리포트를 강제 생성했습니다.", studyId, targetWeek);
        }
    }

    private String generateDbStatsContent(Study study, Long studyId, LocalDateTime startOfWeek, LocalDateTime endOfWeek) {
        long totalNodeCount = studyNodeRepository.countByStudy_Id(studyId);

        List<StudyMaterial> recentMaterials = studyMaterialRepository.findRecentMaterialsWithMember(studyId, startOfWeek, endOfWeek);

        List<StudyNode> activeNodesThisWeek = studyNodeRepository.findActiveNodesBetween(studyId, startOfWeek, endOfWeek);

        int targetWeek = (int) ChronoUnit.WEEKS.between(study.getStartDate(), startOfWeek) + 1;

        List<StudyNode> newNodes = activeNodesThisWeek.stream()
                .filter(n -> n.getActivationWeek() == targetWeek)
                .toList();

        List<StudyNode> coreNodes = activeNodesThisWeek.stream()
                .filter(n -> n.getActivationWeek() < targetWeek && n.getActiveLevel() > 0)
                .toList();

        List<Question> recentQuestions = questionRepository.findByStudyIdAndCreatedAtBetween(studyId, startOfWeek, endOfWeek);

        return buildStatsString(studyId, totalNodeCount, newNodes, coreNodes, recentMaterials, recentQuestions);
    }
    
    private String buildStatsString(Long studyId, long totalNodeCount, List<StudyNode> newNodes, List<StudyNode> coreNodes, List<StudyMaterial> recentMaterials, List<Question> recentQuestions) {
        StringBuilder sb = new StringBuilder();
        sb.append("- 누적 총 노드 수: ").append(totalNodeCount).append("개\n");
        sb.append("- 이번 주 생성된 신규/보강 노드 수: ").append(newNodes.size()).append("개\n");
        sb.append("- 이번 주 핵심 노드(활성도 높은 노드): ");
        coreNodes.forEach(n -> sb.append(n.getTitle()).append("(").append(n.getActiveLevel()).append("), "));
        if (coreNodes.isEmpty()) sb.append("없음");
        sb.append("\n");

        sb.append("- 멤버별 업로드 현황 및 주요 자료 내용:\n");
        Map<String, Long> materialCountByMember = recentMaterials.stream()
                .filter(m -> m.getMemberStudy() != null && m.getMemberStudy().getMember() != null && m.getMemberStudy().getMember().getName() != null)
                .collect(Collectors.groupingBy(m -> m.getMemberStudy().getMember().getName(), Collectors.counting()));
        Map<String, Long> questionCountByMember = recentQuestions.stream()
                .collect(Collectors.groupingBy(q -> q.getMemberName() != null ? q.getMemberName() : "알 수 없음", Collectors.counting()));
        
        List<String> allStudyMembers = memberStudyRepository.findMemberNamesByStudyId(studyId);

        Set<String> allMembers = new HashSet<>(allStudyMembers);
        allMembers.addAll(materialCountByMember.keySet());
        allMembers.addAll(questionCountByMember.keySet());

        if (allMembers.isEmpty()) sb.append("  * 이번 주 활동 없음\n");
        for (String member : allMembers) {
            long materials = materialCountByMember.getOrDefault(member, 0L);
            long questions = questionCountByMember.getOrDefault(member, 0L);
            sb.append("  * [").append(member).append("] 님의 활동 요약: 자료 ").append(materials).append("개, 질문 ").append(questions).append("개\n");
            
            List<StudyMaterial> memberMaterials = recentMaterials.stream()
                    .filter(m -> m.getMemberStudy() != null && m.getMemberStudy().getMember() != null && member.equals(m.getMemberStudy().getMember().getName()))
                    .toList();
            
            if (!memberMaterials.isEmpty()) {
                sb.append("    [업로드한 자료 상세 내용]\n");
                for (StudyMaterial m : memberMaterials) {
                    String title = m.getDataTitle() != null ? m.getDataTitle() : "제목 없음";
                    String content = m.getContent() != null ? m.getContent() : "내용 없음";
                    if (content.length() > 1500) {
                        content = content.substring(0, 1500) + "...(중략)";
                    }
                    sb.append("      - 제목: ").append(title).append("\n");
                    sb.append("      - 내용: ").append(content).append("\n");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public List<Long> getActiveStudyIds() {
        return studyRepository.findIdsByIsActiveTrue();
    }

    @Transactional
    public void markReportAsRead(Long studyId, Long reportId, Long memberId) {
        com.stology.be.domain.report.entity.ReportReadHistory history = reportReadHistoryRepository.findByMemberIdAndStudyId(memberId, studyId)
                .orElseGet(() -> com.stology.be.domain.report.entity.ReportReadHistory.builder()
                        .memberId(memberId)
                        .studyId(studyId)
                        .lastReadReportId(0L)
                        .build());
        
        // Only update if the new report ID is greater (to prevent older reports from marking newer as unread)
        if (history.getLastReadReportId() < reportId) {
            history.updateLastReadReportId(reportId);
            reportReadHistoryRepository.save(history);
        }
    }

    public UnreadReportResponse checkUnreadReport(Long studyId, Long memberId) {
        List<Report> reports = reportRepository.findAllByStudyIdOrderByCreatedAtAsc(studyId);
        if (reports.isEmpty()) {
            return new UnreadReportResponse(false, null);
        }
        
        Report latestReport = reports.get(reports.size() - 1);
        
        com.stology.be.domain.report.entity.ReportReadHistory history = reportReadHistoryRepository.findByMemberIdAndStudyId(memberId, studyId)
                .orElse(null);
                
        if (history == null || history.getLastReadReportId() < latestReport.getId()) {
            return new UnreadReportResponse(true, latestReport.getId());
        }
        
        return new UnreadReportResponse(false, null);
    }
}
