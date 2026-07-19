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

    /**
     * imageUrl: DB에 저장된 정규 S3 URL. 수정 요청 시 이 값을 그대로 되돌려주면 이미지가 유지된다.
     * displayUrl: 화면 렌더링용 presigned URL(1시간 만료). 저장하거나 되돌려보내지 말 것.
     */
    public record ImageInfo(
            Long imageId,
            String imageUrl,
            String displayUrl
    ) {}

    public record AnswerDetail(
            Long answerId,
            String authorName,
            LocalDateTime createdAt,
            String content,
            List<ImageInfo> images,
            Boolean isMine
    ) {}

    public record QuestionDetail(
            Long questionId,
            String title,
            String content,
            String authorName,
            LocalDateTime createdAt,
            List<ImageInfo> images,
            List<AnswerDetail> answerList,
            Boolean studyEnded,
            Boolean isMine
    ) {}

    public record WriteQuestionResult(Long questionId) {}

    public record UpdateQuestionResult(Long questionId) {}

    public record WriteAnswerResult(Long answerId) {}

    public record UpdateAnswerResult(Long answerId) {}

    public record UploadedImage(Long imageId, String imageUrl, String displayUrl) {}

    public record UploadImageResult(List<UploadedImage> images) {}

    /**
     * 선업로드 결과. 아직 어떤 질문/답글에도 연결되지 않아 imageId가 없다.
     */
    public record StagedImage(String imageUrl, String displayUrl) {}

    public record StageImageResult(List<StagedImage> images) {}
}
