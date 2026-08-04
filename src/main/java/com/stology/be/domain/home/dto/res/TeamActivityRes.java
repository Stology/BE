package com.stology.be.domain.home.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stology.be.domain.home.enums.TeamActivityType;
import com.stology.be.global.PageInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TeamActivityRes {

    private PageInfo<String> pageInfo;
    private List<TeamActivityInfo> activities;

    @Getter
    @Builder
    public static class TeamActivityInfo {

        private TeamActivityType activityType;

        private Long studyId;
        private String studyName;

        // 프론트 이동에 사용할 대상 ID
        private Long targetId;

        private String event;
        private LocalDateTime occurredAt;

        // 커서 정렬에만 사용하고 응답에서는 숨김
        @JsonIgnore
        private Long cursorId;
    }
}