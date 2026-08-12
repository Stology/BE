package com.stology.be.domain.inquiry.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 질문/답글 작성·수정 요청(multipart/form-data). @ModelAttribute로 title/content/images가 폼 필드로 바인딩된다.
 * 이미지 없이 보낼 때 Swagger가 images에 빈 문자열을 넣어도, 컨트롤러 @InitBinder가 빈 파일 값을 무시한다.
 *
 * <p>content 토큰 규약: 저장/조회는 {@code [[img:{imageId}]]}, 요청의 새 파일은 {@code [[img:new:K]]}(images의 K번째).
 * 서버가 저장 후 new:K를 실제 imageId로 치환한다.
 */
public class InquiryReqDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class WriteQuestion {
        private String title;
        private String content;

        @ArraySchema(schema = @Schema(type = "string", format = "binary"))
        private List<MultipartFile> images;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UpdateQuestion {
        private String title;
        private String content;

        @ArraySchema(schema = @Schema(type = "string", format = "binary"))
        private List<MultipartFile> images;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class WriteAnswer {
        private String content;

        @ArraySchema(schema = @Schema(type = "string", format = "binary"))
        private List<MultipartFile> images;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UpdateAnswer {
        private String content;

        @ArraySchema(schema = @Schema(type = "string", format = "binary"))
        private List<MultipartFile> images;
    }
}
