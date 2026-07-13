package com.stology.be.domain.node.dto.res;

import com.stology.be.domain.node.entity.StudyMaterial;


import java.time.LocalDateTime;
import java.util.List;

public record NodeInfoRes(
        Long studyNodeId,
        List<MaterialInfo> materials
) {

    public static NodeInfoRes of(
            Long studyNodeId,
            List<MaterialInfo> materials
    ) {
        return new NodeInfoRes(
                studyNodeId,
                materials
        );
    }

    public record MaterialInfo(
            Long studyMaterialId,
            String dataTitle,
            String fileUrl,
            String uploaderName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        public static MaterialInfo from(StudyMaterial studyMaterial) {
            return new MaterialInfo(
                    studyMaterial.getId(),
                    studyMaterial.getDataTitle(),
                    studyMaterial.getFileUrl(),
                    studyMaterial.getMemberStudy()
                            .getMember()
                            .getName(),
                    studyMaterial.getCreatedAt(),
                    studyMaterial.getUpdatedAt()
            );
        }
    }
}