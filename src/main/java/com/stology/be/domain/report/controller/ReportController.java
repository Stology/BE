package com.stology.be.domain.report.controller;

import com.stology.be.domain.report.dto.response.ReportResponseDto.*;
import com.stology.be.domain.report.service.ReportService;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.apiPayload.code.GeneralSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "Report API", description = "주차별 학습 리포트 관련 API")
@RestController
@RequestMapping("/api/study/{studyId}/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @Operation(summary = "리포트 요약 통계 조회", description = "특정 주차의 노드 개수 및 신규/보강 퍼센트 등 리포트 상단 요약 통계를 조회합니다.")
    @GetMapping
    public ApiResponse<ReportSummaryResponse> getReportSummary(
            @Parameter(description = "스터디 ID") @PathVariable Long studyId,
            @Parameter(description = "조회할 주차 (미입력 시 최신 주차)") @RequestParam(required = false) Integer week) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, reportService.getReportSummary(studyId, week));
    }

    @Operation(summary = "이번 주 핵심 노드 목록 조회", description = "이번 주 리포트의 핵심 학습 노드 목록(신규/보강)을 조회합니다.")
    @GetMapping("/node/week")
    public ApiResponse<WeeklyCoreNodeResponse> getWeeklyCoreNode(
            @Parameter(description = "스터디 ID") @PathVariable Long studyId,
            @Parameter(description = "조회할 주차 (미입력 시 최신 주차)") @RequestParam(required = false) Integer week) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, reportService.getWeeklyCoreNode(studyId, week));
    }

    @Operation(summary = "AI 종합 총평 조회", description = "해당 리포트의 AI 종합 학습 총평 내용을 조회합니다.")
    @GetMapping("/ai-review/{aiReviewId}")
    public ApiResponse<AiReviewResponse> getAiReview(
            @Parameter(description = "스터디 ID") @PathVariable Long studyId,
            @Parameter(description = "AI 리뷰 ID") @PathVariable Long aiReviewId) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, reportService.getAiReview(studyId, aiReviewId));
    }

    @Operation(summary = "다음 주 추천 노드 조회", description = "이번 주 학습을 기반으로 AI가 추천하는 다음 주 학습 노드 목록을 조회합니다.")
    @GetMapping("/node/recommend")
    public ApiResponse<RecommendedNodeResponse> getRecommendedNode(
            @Parameter(description = "스터디 ID") @PathVariable Long studyId,
            @Parameter(description = "조회할 주차 (미입력 시 최신 주차)") @RequestParam(required = false) Integer week) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, reportService.getRecommendedNode(studyId, week));
    }

    @Operation(summary = "멤버별 활동 통계 조회", description = "스터디원들의 이번 주 질문 수, 자료 업로드 수 및 AI 피드백 통계를 조회합니다.")
    @GetMapping("/statistics")
    public ApiResponse<MemberActivityStatisticsResponse> getStatistics(
            @Parameter(description = "스터디 ID") @PathVariable Long studyId,
            @Parameter(description = "조회할 주차 (미입력 시 최신 주차)") @RequestParam(required = false) Integer week) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, reportService.getStatistics(studyId, week));
    }

    @Operation(summary = "주차별 전체 리포트 렌더링용 종합 조회", description = "리포트 화면을 그리기 위해 필요한 전체 리포트 정보(상단 통계, 핵심 노드, 총평, 추천 노드, 멤버 통계 등)를 한 번에 조회합니다. 호출 시 자동으로 해당 사용자의 읽음 처리가 진행됩니다.")
    @GetMapping("/all")
    public ApiResponse<FullReportResponse> getFullReport(
            @Parameter(description = "스터디 ID") @PathVariable Long studyId,
            @Parameter(description = "조회할 주차 (미입력 시 최신 주차)") @RequestParam(required = false) Integer week,
            @AuthenticationPrincipal com.stology.be.global.security.entity.AuthMember authMember) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, reportService.getFullReport(studyId, week, authMember.getMember().getId()));
    }

    @Operation(summary = "[테스트용] 리포트 강제 삭제", description = "지정된 ID의 리포트를 강제로 삭제합니다. (테스트 목적)")
    @DeleteMapping("/test/{reportId}")
    public ApiResponse<String> deleteReport(
            @Parameter(description = "스터디 ID") @PathVariable Long studyId,
            @Parameter(description = "삭제할 리포트 ID") @PathVariable Long reportId) {
        reportService.deleteReport(reportId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, "(테스트용) 리포트가 성공적으로 삭제되었습니다.");
    }

    @Operation(summary = "[테스트용] 리포트 수동 갱신 스케줄러 트리거", description = "스터디의 시작일과 현재 시간을 비교하여 누락된 리포트들을 강제로 일괄 생성합니다. (테스트 목적)")
    @PostMapping("/test/update")
    public ApiResponse<String> triggerReportUpdate(
            @Parameter(description = "스터디 ID") @PathVariable Long studyId) {
        reportService.checkAndGenerateMissingReports(studyId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, "(테스트용) 리포트 수동 갱신이 완료되었습니다.");
    }

    @Operation(summary = "미확인 신규 리포트 알림 배지 조회", description = "현재 사용자가 아직 읽지 않은 신규 리포트가 존재하는지(hasUnread)와 가장 최신 미확인 리포트 ID를 조회합니다.")
    @GetMapping("/unread")
    public ApiResponse<UnreadReportResponse> getUnreadReport(
            @Parameter(description = "스터디 ID") @PathVariable Long studyId,
            @AuthenticationPrincipal com.stology.be.global.security.entity.AuthMember authMember) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, reportService.checkUnreadReport(studyId, authMember.getMember().getId()));
    }
}


