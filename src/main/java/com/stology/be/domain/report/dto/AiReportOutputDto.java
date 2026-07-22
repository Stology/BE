package com.stology.be.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiReportOutputDto {

    private String aiReviewContent;
    private List<RecommendedNodeDto> recommendedNodeList;
    private List<MemberActivityStatisticsDto> memberActivityStatisticsList;
}
