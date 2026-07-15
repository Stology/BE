package com.stology.be.domain.inquiry.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class InquiryResDTO {

    public record QuestionSummary(
            Long questionId,
            String title,
            String authorName,
            LocalDateTime createdAt,
            Integer answerCount,
            Boolean hasImage,
            Boolean isMine
    ) {}

    public record QuestionList(
            List<QuestionSummary> questionList,
            Integer listSize,
            Integer totalPage,
            Long totalElements,
            Boolean isFirst,
            Boolean isLast,
            Boolean studyEnded
    ) {}

    public record AnswerDetail(
            Long answerId,
            String authorName,
            LocalDateTime createdAt,
            String content,
            List<String> imageUrls,
            Boolean isMine
    ) {}

    public record QuestionDetail(
            Long questionId,
            String title,
            String content,
            String authorName,
            LocalDateTime createdAt,
            List<String> imageUrls,
            List<AnswerDetail> answerList,
            Boolean studyEnded,
            Boolean isMine
    ) {}

    public record WriteQuestionResult(Long questionId) {}

    public record UpdateQuestionResult(Long questionId) {}

    public record WriteAnswerResult(Long answerId) {}

    public record UpdateAnswerResult(Long answerId) {}

    public record UploadedImage(Long imageId, String imageUrl) {}

    public record UploadImageResult(List<UploadedImage> images) {}
}
