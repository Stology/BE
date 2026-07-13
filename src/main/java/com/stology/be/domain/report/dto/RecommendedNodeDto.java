package com.stology.be.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedNodeDto {
    private String nodeName;
    private String reason;
    private String badge;
}
