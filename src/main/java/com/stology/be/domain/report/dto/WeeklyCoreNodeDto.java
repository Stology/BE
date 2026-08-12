package com.stology.be.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyCoreNodeDto {
    private String nodeName;
    private String state;
    private Integer materialCount;
}
