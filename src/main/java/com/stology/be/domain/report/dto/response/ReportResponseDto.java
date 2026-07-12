package com.stology.be.domain.report.dto.response;

import com.stology.be.domain.report.dto.MemberActivityStatisticsDto;
import com.stology.be.domain.report.dto.RecommendedNodeDto;
import com.stology.be.domain.report.dto.WeeklyCoreNodeDto;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

public class ReportResponseDto {

    @Getter
    @Builder
    public static class ReportSummaryResponse {
        private Long reportId;
        private Integer totalNodeCount;
        private Integer newActiveNodeCount;
        private Integer newActiveNodePercentage;
        private Integer reinforcedNodeCount;
        private Integer reinforcedNodePercentage;
    }

    @Getter
    @Builder
    public static class WeeklyCoreNodeResponse {
        private List<WeeklyCoreNodeDto> weeklyCoreNodeList;
    }

    @Getter
    @Builder
    public static class AiReviewResponse {
        private String aiReviewContent;
    }

    @Getter
    @Builder
    public static class RecommendedNodeResponse {
        private List<RecommendedNodeDto> recommendedNodeList;
    }

    @Getter
    @Builder
    public static class FollowUpResponse {
        private String followUpContent;
    }

    @Getter
    @Builder
    public static class MemberActivityStatisticsResponse {
        private List<MemberActivityStatisticsDto> memberActivityStatisticsList;
    }
}
