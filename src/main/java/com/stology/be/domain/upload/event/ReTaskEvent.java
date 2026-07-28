package com.stology.be.domain.upload.event;

import lombok.Builder;

@Builder
public record ReTaskEvent(
        Long studyId,
        Long studyMaterialId,
        Long uploaderMemberId
) {
}
