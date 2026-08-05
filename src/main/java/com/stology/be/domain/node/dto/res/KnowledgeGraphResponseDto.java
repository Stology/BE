package com.stology.be.domain.node.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class KnowledgeGraphResponseDto {

    @Getter
    @Builder
    public static class GraphRes {
        private List<NodeDto> nodes;
        private List<EdgeDto> edges;
    }

    @Getter
    @Builder
    public static class NodeDto {
        private Long id;
        private String title;
        private String description;
        private Integer activeLevel;
        private Integer activationWeek;
        private Integer recommendWeek;
    }

    @Getter
    @Builder
    public static class EdgeDto {
        private Long source;
        private Long target;
        private String relation;
    }

    @Getter
    @Builder
    public static class NodeDetailRes {
        private Long nodeId;
        private String title;
        private String definition;
        private Integer activeLevel;
        @JsonProperty("isActive")
        private Boolean isActive;
        private Integer materialCount;
        private List<MaterialDto> recentMaterials;
        private Map<String, List<ConnectedNodeDto>> relations;
    }

    @Getter
    @Builder
    public static class ConnectedNodeDto {
        private Long nodeId;
        private String title;
        private Integer activeLevel;
    }

    @Getter
    @Builder
    public static class MaterialDto {
        private Long id;
        private String title;
        private String memberName;
        private LocalDateTime createdAt;
    }
}
