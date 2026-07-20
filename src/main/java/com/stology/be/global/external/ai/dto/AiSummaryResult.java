package com.stology.be.global.external.ai.dto;

import java.util.List;

public record AiSummaryResult(
        String summary,
        List<String> keywords
) {
}