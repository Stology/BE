package com.stology.be.domain.upload.dto.res;
import com.stology.be.domain.upload.enums.DataState;

import java.time.LocalDateTime;

public record UploadSseRes(
        Long studyId,
        Long studyMaterialId,
        Long uploaderMemberId,
        String uploaderName,
        String dataTitle,
        DataState dataState,
        LocalDateTime createdAt
) {
}
