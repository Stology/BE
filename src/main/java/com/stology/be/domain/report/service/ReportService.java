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
    private final com.stology.be.domain.node.repository.StudyMaterialRepository studyMaterialRepository;
    private final com.stology.be.domain.study.repository.StudyRepository studyRepository;
    private final AiReportService aiReportService;
    
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

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

        // 3. 최근 변경 시간이 기존 리포트 생성 시간보다 나중이거나, 기존 리포트가 실패(임시) 리포트라면 갱신 시작
        boolean isFallbackReport = currentReport != null && currentReport.getAiReviewContent() != null && currentReport.getAiReviewContent().contains("AI 통신에 실패하여");
        if (latestActivityTime.isAfter(lastReportTime) || isFallbackReport) {
            log.info("새로운 수정/생성 활동이 감지되었습니다. (최신 활동: {}, 기존 리포트 생성: {}) Groq 갱신을 시작합니다.", latestActivityTime, lastReportTime);
            
            if(currentReport == null) {
               throw new GeneralException(GeneralErrorCode.NOT_FOUND); // 첫 리포트가 없는 경우는 다른 플로우(ex: 첫 가입)에서 처리해야 함
            }
            
            com.stology.be.domain.report.dto.AiReportOutputDto output = aiReportService.generateNewReport(currentReport.getStudy(), generateDbStatsContent(currentReport.getStudy(), currentReport.getStudy().getId()));
            
            // Build weeklyCoreNodeList manually based on actual database values, avoiding AI hallucination
            int currentWeek = 1;
            if (currentReport.getStudy().getStartDate() != null) {
                currentWeek = (int) java.time.temporal.ChronoUnit.WEEKS.between(currentReport.getStudy().getStartDate(), java.time.LocalDate.now()) + 1;
            }
            
            java.time.LocalDateTime startOfWeek = java.time.LocalDateTime.now().minusDays(7);
            if (currentReport.getStudy().getStartDate() != null) {
                startOfWeek = currentReport.getStudy().getStartDate().plusWeeks(currentWeek - 1).atStartOfDay();
            }
            
            List<com.stology.be.domain.node.entity.StudyNode> activeNodesThisWeek = entityManager.createQuery(
                    "SELECT DISTINCT n FROM StudyNode n " +
                    "JOIN NodeCandidate nc ON nc.studyNode.id = n.id " +
                    "JOIN nc.studyMaterial m " +
                    "WHERE m.createdAt >= :startOfWeek AND n.study.id = :studyId",
                    com.stology.be.domain.node.entity.StudyNode.class)
                    .setParameter("studyId", currentReport.getStudy().getId())
                    .setParameter("startOfWeek", startOfWeek)
                    .getResultList();
                    
            final int targetCurrentWeek = currentWeek;
            List<com.stology.be.domain.node.entity.StudyNode> newNodes = activeNodesThisWeek.stream()
                    .filter(n -> n.getActivationWeek() == targetCurrentWeek)
                    .toList();
    
            List<com.stology.be.domain.node.entity.StudyNode> coreNodes = activeNodesThisWeek.stream()
                    .filter(n -> n.getActivationWeek() < targetCurrentWeek && n.getActiveLevel() > 0)
                    .toList();
                    
            List<com.stology.be.domain.report.dto.WeeklyCoreNodeDto> coreNodeDtoList = new java.util.ArrayList<>();
            for (com.stology.be.domain.node.entity.StudyNode node : newNodes) {
                coreNodeDtoList.add(new com.stology.be.domain.report.dto.WeeklyCoreNodeDto(node.getTitle(), "신규 활성화", node.getActiveLevel()));
            }
            for (com.stology.be.domain.node.entity.StudyNode node : coreNodes) {
                coreNodeDtoList.add(new com.stology.be.domain.report.dto.WeeklyCoreNodeDto(node.getTitle(), "활성", node.getActiveLevel()));
            }

            currentReport.update(
                    studyNodeRepository.countByStudy_Id(currentReport.getStudy().getId()).intValue(),
                    newNodes.size(),
                    coreNodes.size(),
                    coreNodeDtoList,
                    output.getAiReviewContent(),
                    output.getRecommendedNodeList(),
                    output.getMemberActivityStatisticsList()
            );
                    
            return reportRepository.save(currentReport);
        }
        
        return currentReport;
    }

    private String generateDbStatsContent(com.stology.be.domain.study.entity.Study study, Long studyId) {
        int currentWeek = 1;
        if (study.getStartDate() != null) {
            currentWeek = (int) java.time.temporal.ChronoUnit.WEEKS.between(study.getStartDate(), java.time.LocalDate.now()) + 1;
        }

        long totalNodeCount = entityManager.createQuery("SELECT COUNT(n) FROM StudyNode n WHERE n.study.id = :studyId", Long.class)
                .setParameter("studyId", studyId)
                .getSingleResult();
        
        java.time.LocalDateTime startOfWeek = java.time.LocalDateTime.now().minusDays(7);
        if (study.getStartDate() != null) {
            startOfWeek = study.getStartDate().plusWeeks(currentWeek - 1).atStartOfDay();
        }

        // 이번 주에 등록된 학습 자료 조회
        List<com.stology.be.domain.node.entity.StudyMaterial> recentMaterials = entityManager.createQuery(
                "SELECT m FROM StudyMaterial m JOIN m.memberStudy ms JOIN ms.study s WHERE s.id = :studyId AND m.createdAt >= :startOfWeek", 
                com.stology.be.domain.node.entity.StudyMaterial.class)
                .setParameter("studyId", studyId)
                .setParameter("startOfWeek", startOfWeek)
                .getResultList();

        // 이번 주에 활동(자료 업로드)이 있는 노드들 추출
        List<com.stology.be.domain.node.entity.StudyNode> activeNodesThisWeek = entityManager.createQuery(
                "SELECT DISTINCT n FROM StudyNode n " +
                "JOIN NodeCandidate nc ON nc.studyNode.id = n.id " +
                "JOIN nc.studyMaterial m " +
                "WHERE m.createdAt >= :startOfWeek AND n.study.id = :studyId",
                com.stology.be.domain.node.entity.StudyNode.class)
                .setParameter("studyId", studyId)
                .setParameter("startOfWeek", startOfWeek)
                .getResultList();

        // 신규 노드 (원래 활성도 0 -> 이번 주 활동) 와 보강 노드 (기존 활성도 > 0 -> 이번 주 활동) 분리
        // DB 구조상 현재 activeLevel이 이미 반영되어 있다면, activationWeek가 현재 주차(currentWeek)인 것을 신규 노드로 판단.
        final int targetCurrentWeek = currentWeek;
        List<com.stology.be.domain.node.entity.StudyNode> newNodes = activeNodesThisWeek.stream()
                .filter(n -> n.getActivationWeek() == targetCurrentWeek)
                .toList();

        List<com.stology.be.domain.node.entity.StudyNode> coreNodes = activeNodesThisWeek.stream()
                .filter(n -> n.getActivationWeek() < targetCurrentWeek && n.getActiveLevel() > 0)
                .toList();

        List<com.stology.be.domain.study.entity.Question> recentQuestions = entityManager.createQuery(
                "SELECT q FROM Question q WHERE q.study.id = :studyId AND q.createdAt >= :startOfWeek", 
                com.stology.be.domain.study.entity.Question.class)
                .setParameter("studyId", studyId)
                .setParameter("startOfWeek", startOfWeek)
                .getResultList();

        StringBuilder sb = new StringBuilder();
        sb.append("- 누적 총 노드 수: ").append(totalNodeCount).append("개\n");
        sb.append("- 이번 주 생성된 신규/보강 노드 수: ").append(newNodes.size()).append("개\n");
        sb.append("- 이번 주 핵심 노드(활성도 높은 노드): ");
        coreNodes.forEach(n -> sb.append(n.getTitle()).append("(").append(n.getActiveLevel()).append("), "));
        if (coreNodes.isEmpty()) sb.append("없음");
        sb.append("\n");

        sb.append("- 멤버별 업로드 현황 및 주요 자료 내용:\n");
        java.util.Map<String, Long> materialCountByMember = recentMaterials.stream()
                .filter(m -> m.getMemberStudy() != null && m.getMemberStudy().getMember() != null && m.getMemberStudy().getMember().getName() != null)
                .collect(java.util.stream.Collectors.groupingBy(m -> m.getMemberStudy().getMember().getName(), java.util.stream.Collectors.counting()));
        java.util.Map<String, Long> questionCountByMember = recentQuestions.stream()
                .collect(java.util.stream.Collectors.groupingBy(q -> q.getMemberName() != null ? q.getMemberName() : "알 수 없음", java.util.stream.Collectors.counting()));
        
        java.util.Set<String> allMembers = new java.util.HashSet<>();
        allMembers.addAll(materialCountByMember.keySet());
        allMembers.addAll(questionCountByMember.keySet());

        if (allMembers.isEmpty()) sb.append("  * 이번 주 활동 없음\n");
        for (String member : allMembers) {
            long materials = materialCountByMember.getOrDefault(member, 0L);
            long questions = questionCountByMember.getOrDefault(member, 0L);
            sb.append("  * [").append(member).append("] 님의 활동 요약: 자료 ").append(materials).append("개, 질문 ").append(questions).append("개\n");
            
            // 해당 멤버가 업로드한 자료 내용 추가
            java.util.List<com.stology.be.domain.node.entity.StudyMaterial> memberMaterials = recentMaterials.stream()
                    .filter(m -> m.getMemberStudy() != null && m.getMemberStudy().getMember() != null && member.equals(m.getMemberStudy().getMember().getName()))
                    .toList();
            
            if (!memberMaterials.isEmpty()) {
                sb.append("    [업로드한 자료 상세 내용]\n");
                for (com.stology.be.domain.node.entity.StudyMaterial m : memberMaterials) {
                    String title = m.getDataTitle() != null ? m.getDataTitle() : "제목 없음";
                    String content = m.getContent() != null ? m.getContent() : "내용 없음";
                    // 너무 긴 텍스트 방지 (1500자 제한)
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

    public Report getReportByWeek(Long studyId, Integer week) {
        List<Report> reports = reportRepository.findAllByStudyIdOrderByCreatedAtAsc(studyId);
        
        Report targetReport;
        if (reports.isEmpty()) {
            // 첫 리포트가 없는 경우 즉시 새로 생성
            com.stology.be.domain.study.entity.Study study = studyRepository.findById(studyId)
                    .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND));
            com.stology.be.domain.report.dto.AiReportOutputDto output = aiReportService.generateNewReport(study, generateDbStatsContent(study, studyId));
            
            int currentWeek = 1;
            if (study.getStartDate() != null) {
                currentWeek = (int) java.time.temporal.ChronoUnit.WEEKS.between(study.getStartDate(), java.time.LocalDate.now()) + 1;
            }
            java.time.LocalDateTime startOfWeek = java.time.LocalDateTime.now().minusDays(7);
            if (study.getStartDate() != null) {
                startOfWeek = study.getStartDate().plusWeeks(currentWeek - 1).atStartOfDay();
            }
            List<com.stology.be.domain.node.entity.StudyNode> activeNodesThisWeek = entityManager.createQuery(
                    "SELECT DISTINCT n FROM StudyNode n " +
                    "JOIN NodeCandidate nc ON nc.studyNode.id = n.id " +
                    "JOIN nc.studyMaterial m " +
                    "WHERE m.createdAt >= :startOfWeek AND n.study.id = :studyId",
                    com.stology.be.domain.node.entity.StudyNode.class)
                    .setParameter("studyId", studyId)
                    .setParameter("startOfWeek", startOfWeek)
                    .getResultList();
                    
            final int targetCurrentWeek = currentWeek;
            List<com.stology.be.domain.node.entity.StudyNode> newNodes = activeNodesThisWeek.stream()
                    .filter(n -> n.getActivationWeek() == targetCurrentWeek)
                    .toList();
    
            List<com.stology.be.domain.node.entity.StudyNode> coreNodes = activeNodesThisWeek.stream()
                    .filter(n -> n.getActivationWeek() < targetCurrentWeek && n.getActiveLevel() > 0)
                    .toList();
                    
            List<com.stology.be.domain.report.dto.WeeklyCoreNodeDto> coreNodeDtoList = new java.util.ArrayList<>();
            for (com.stology.be.domain.node.entity.StudyNode node : newNodes) {
                coreNodeDtoList.add(new com.stology.be.domain.report.dto.WeeklyCoreNodeDto(node.getTitle(), "신규 활성화", node.getActiveLevel()));
            }
            for (com.stology.be.domain.node.entity.StudyNode node : coreNodes) {
                coreNodeDtoList.add(new com.stology.be.domain.report.dto.WeeklyCoreNodeDto(node.getTitle(), "활성", node.getActiveLevel()));
            }

            Report newReport = Report.builder()
                    .study(study)
                    .totalNodeCount(studyNodeRepository.countByStudy_Id(studyId).intValue())
                    .newActiveNodeCount(newNodes.size())
                    .reinforcedNodeCount(coreNodes.size())
                    .weeklyCoreNodeList(coreNodeDtoList)
                    .aiReviewContent(output.getAiReviewContent())
                    .recommendedNodeList(output.getRecommendedNodeList())
                    .memberActivityStatisticsList(output.getMemberActivityStatisticsList())
                    .build();
            targetReport = reportRepository.save(newReport);
        } else {
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

    public MemberActivityStatisticsResponse getStatistics(Long studyId, Integer week) {
        Report report = getReportByWeek(studyId, week);
        return MemberActivityStatisticsResponse.builder()
                .memberActivityStatisticsList(report.getMemberActivityStatisticsList())
                .build();
    }

    public FullReportResponse getFullReport(Long studyId, Integer week) {
        Report report = getReportByWeek(studyId, week);

        int totalNewAndReinforced = report.getNewActiveNodeCount() + report.getReinforcedNodeCount();
        int newPercentage = 0;
        int reinforcedPercentage = 0;

        if (totalNewAndReinforced > 0) {
            newPercentage = (int) Math.round((double) report.getNewActiveNodeCount() / totalNewAndReinforced * 100);
            reinforcedPercentage = 100 - newPercentage;
        }

        return FullReportResponse.builder()
                .reportId(report.getId())
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

    @org.springframework.transaction.annotation.Transactional
    public void deleteReport(Long reportId) {
        reportRepository.deleteById(reportId);
    }

    public void checkAndGenerateMissingReports(Long studyId) {
        com.stology.be.domain.study.entity.Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND));

        if (study.getStartDate() == null) return;

        int currentWeek = (int) java.time.temporal.ChronoUnit.WEEKS.between(study.getStartDate(), java.time.LocalDate.now()) + 1;
        List<Report> reports = reportRepository.findAllByStudyIdOrderByCreatedAtAsc(studyId);

        // 스터디 시작일로부터 계산된 currentWeek 만큼 리포트가 존재해야 함.
        // ex: 2주차가 진행 중이면, 1주차 리포트는 이미 완성(생성)되어 있어야 함. (현재 리포트는 매주 온디맨드로 수정되므로, 최신 주차 리포트도 사실상 존재함)
        // 리포트 개수가 현재 주차보다 적다면, 누락된 주차 리포트를 강제 생성하여 채워넣음.
        int missingCount = currentWeek - reports.size();
        for (int i = 0; i < missingCount; i++) {
            int targetWeek = reports.size() + 1; // 누락된 주차 번호
            
            // Generate for targetWeek
            java.time.LocalDateTime startOfWeek = study.getStartDate().plusWeeks(targetWeek - 1).atStartOfDay();
            java.time.LocalDateTime endOfWeek = startOfWeek.plusDays(7); // 이 시점까지의 데이터만 집계

            com.stology.be.domain.report.dto.AiReportOutputDto output = aiReportService.generateNewReport(study, generateDbStatsContent(study, studyId));
            
            List<com.stology.be.domain.node.entity.StudyNode> activeNodesThisWeek = entityManager.createQuery(
                    "SELECT DISTINCT n FROM StudyNode n " +
                    "JOIN NodeCandidate nc ON nc.studyNode.id = n.id " +
                    "JOIN nc.studyMaterial m " +
                    "WHERE m.createdAt >= :startOfWeek AND m.createdAt < :endOfWeek AND n.study.id = :studyId",
                    com.stology.be.domain.node.entity.StudyNode.class)
                    .setParameter("studyId", studyId)
                    .setParameter("startOfWeek", startOfWeek)
                    .setParameter("endOfWeek", endOfWeek)
                    .getResultList();
                    
            List<com.stology.be.domain.node.entity.StudyNode> newNodes = activeNodesThisWeek.stream()
                    .filter(n -> n.getActivationWeek() == targetWeek)
                    .toList();
    
            List<com.stology.be.domain.node.entity.StudyNode> coreNodes = activeNodesThisWeek.stream()
                    .filter(n -> n.getActivationWeek() < targetWeek && n.getActiveLevel() > 0)
                    .toList();
                    
            List<com.stology.be.domain.report.dto.WeeklyCoreNodeDto> coreNodeDtoList = new java.util.ArrayList<>();
            for (com.stology.be.domain.node.entity.StudyNode node : newNodes) {
                coreNodeDtoList.add(new com.stology.be.domain.report.dto.WeeklyCoreNodeDto(node.getTitle(), "신규 활성화", node.getActiveLevel()));
            }
            for (com.stology.be.domain.node.entity.StudyNode node : coreNodes) {
                coreNodeDtoList.add(new com.stology.be.domain.report.dto.WeeklyCoreNodeDto(node.getTitle(), "활성", node.getActiveLevel()));
            }

            Report newReport = Report.builder()
                    .study(study)
                    .totalNodeCount(studyNodeRepository.countByStudy_Id(studyId).intValue())
                    .newActiveNodeCount(newNodes.size())
                    .reinforcedNodeCount(coreNodes.size())
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
}
