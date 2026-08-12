package com.stology.be.domain.home.dto.res;

import lombok.Builder;

@Builder
public record MyTodoRes(
        long reviewCount,
        long reUploadCount,
        long questionCount,
        long answerCount,
        long studyCount
) {
}