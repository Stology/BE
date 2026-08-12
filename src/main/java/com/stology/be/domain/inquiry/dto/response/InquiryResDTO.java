package com.stology.be.domain.inquiry.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public class InquiryResDTO {

    /**
     * createdAt 직렬화 포맷. LocalDateTime을 그대로 내리면 나노초 뒤 0이 잘려 자릿수가 들쭉날쭉해진다
     * (예: .71133 vs .089196). 밀리초 3자리로 고정해 클라이언트 파서 호환성을 확보한다.
     */
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    public record QuestionSummary(
            Long questionId,
            String title,
            String authorName,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
            LocalDateTime createdAt,
            Integer answerCount,
            Boolean hasImage,
            Boolean isMine
    ) {}

    public record QuestionList(
            List<QuestionSummary> questionList,
            Integer currentPage,
            Integer listSize,
            Integer totalPage,
            Long totalElements,
            Boolean isFirst,
            Boolean isLast,
            Boolean studyEnded
    ) {}

    /**
     * imageUrl: DB에 저장된 S3 공개 객체 URL. 화면 렌더링에도 이 URL을 그대로 쓰고,
     * 수정 요청의 imageOrder에 이 값을 담으면 이미지가 유지된다.
     * 프론트는 content의 [[img:N]]을 images[N].imageUrl로 치환해 인라인 렌더링한다.
     */
    public record ImageInfo(
            Long imageId,
            String imageUrl
    ) {}

    public record AnswerDetail(
            Long answerId,
            String authorName,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
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
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
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
}
