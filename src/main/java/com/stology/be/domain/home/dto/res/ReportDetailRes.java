package com.stology.be.domain.home.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReportDetailRes {

    private List<ReportInfo> reports;

    @Getter
    @Builder
    public static class ReportInfo {

        private Long studyId;
        private String studyName;

        private Long reportId;
        private Long reportWeek;
        private LocalDateTime createdAt;

        private boolean generated;
    }
}