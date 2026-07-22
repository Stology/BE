package com.stology.be.domain.report.controller;

import com.stology.be.domain.report.dto.response.ReportResponseDto.*;
import com.stology.be.domain.report.service.ReportService;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.apiPayload.code.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/study/{studyId}/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping
    public ApiResponse<ReportSummaryResponse> getReportSummary(
            @PathVariable Long studyId,
            @RequestParam(required = false) Integer week) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, reportService.getReportSummary(studyId, week));
    }

    @GetMapping("/node/week")
    public ApiResponse<WeeklyCoreNodeResponse> getWeeklyCoreNode(
            @PathVariable Long studyId,
            @RequestParam(required = false) Integer week) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, reportService.getWeeklyCoreNode(studyId, week));
    }

    @GetMapping("/ai-review/{aiReviewId}")
    public ApiResponse<AiReviewResponse> getAiReview(
            @PathVariable Long studyId,
            @PathVariable Long aiReviewId) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, reportService.getAiReview(studyId, aiReviewId));
    }

    @GetMapping("/node/recommend")
    public ApiResponse<RecommendedNodeResponse> getRecommendedNode(
            @PathVariable Long studyId,
            @RequestParam(required = false) Integer week) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, reportService.getRecommendedNode(studyId, week));
    }

    @GetMapping("/statistics")
    public ApiResponse<MemberActivityStatisticsResponse> getStatistics(
            @PathVariable Long studyId,
            @RequestParam(required = false) Integer week) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, reportService.getStatistics(studyId, week));
    }

    @GetMapping("/all")
    public ApiResponse<FullReportResponse> getFullReport(
            @PathVariable Long studyId,
            @RequestParam(required = false) Integer week) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, reportService.getFullReport(studyId, week));
    }

    @DeleteMapping("/{reportId}")
    public ApiResponse<String> deleteReport(
            @PathVariable Long studyId,
            @PathVariable Long reportId) {
        reportService.deleteReport(reportId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, "리포트가 성공적으로 삭제되었습니다.");
    }
}

