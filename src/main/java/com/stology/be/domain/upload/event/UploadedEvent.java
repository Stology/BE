package com.stology.be.domain.upload.event;


import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UploadedEvent(
        Long studyId,
        Long studyMaterialId,
        Long uploaderMemberId,
        String uploaderName,
        String dataTitle,
        Integer week,
        LocalDateTime createdAt
) {
}