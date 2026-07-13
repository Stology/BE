package com.stology.be.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberActivityStatisticsDto {
    private String memberName;
    private Integer materialUploadCount;
    private Integer questionCount;
    private String aiFeedback;
}
