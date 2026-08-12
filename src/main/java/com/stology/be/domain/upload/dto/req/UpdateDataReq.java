package com.stology.be.domain.upload.dto.req;

import jakarta.validation.constraints.NotBlank;

public record UpdateDataReq(
        @NotBlank(message = "자료 제목은 필수입니다.")
        String dataTitle,

        String content
) {
}
