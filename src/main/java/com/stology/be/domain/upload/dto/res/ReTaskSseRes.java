package com.stology.be.domain.upload.dto.res;

import com.stology.be.domain.upload.enums.DataState;
import lombok.Builder;

@Builder
public record ReTaskSseRes (
        Long studyId,
        Long studyMaterialId,
        Long uploaderMemberId,
        DataState dataState
){
}
