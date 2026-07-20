package com.stology.be.domain.upload.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class UploadReq {

    /**
     * 몇 주차 자료인지
     */

    /**
     * 자료 제목
     */
    @NotBlank(message = "자료 제목은 필수입니다.")
    private String title;

    /**
     * 자료 설명
     */
    private String description;

    /**
     * Markdown 파일
     */
    @NotNull(message = "파일은 필수입니다.")
    private MultipartFile file;
}