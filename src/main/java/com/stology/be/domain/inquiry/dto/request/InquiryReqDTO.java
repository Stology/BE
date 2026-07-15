package com.stology.be.domain.inquiry.dto.request;

public class InquiryReqDTO {

    public record WriteQuestion(
            String title,
            String content
    ) {}

    public record UpdateQuestion(
            String title,
            String content
    ) {}

    public record WriteAnswer(
            String content
    ) {}

    public record UpdateAnswer(
            String content
    ) {}
}
