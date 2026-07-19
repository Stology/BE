package com.stology.be.domain.inquiry.dto.request;

import java.util.List;

public class InquiryReqDTO {

    /**
     * imageUrls: 선업로드 API(POST .../question/image)가 내려준 imageUrl 목록.
     * null이거나 비어 있으면 첨부 이미지 없이 등록된다.
     */
    public record WriteQuestion(
            String title,
            String content,
            List<String> imageUrls
    ) {}

    /**
     * imageUrls: 수정 후 남길 이미지의 최종 목록(기존 이미지의 imageUrl + 새로 올린 imageUrl).
     * null이면 이미지를 건드리지 않고, 빈 리스트면 첨부 이미지를 전부 제거한다.
     */
    public record UpdateQuestion(
            String title,
            String content,
            List<String> imageUrls
    ) {}

    public record WriteAnswer(
            String content,
            List<String> imageUrls
    ) {}

    public record UpdateAnswer(
            String content,
            List<String> imageUrls
    ) {}
}
