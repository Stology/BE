package com.stology.be.domain.home.dto;

import java.time.LocalDateTime;

public record NodeActivityKey(
        Long studyId,
        LocalDateTime occurredAt
) {
}