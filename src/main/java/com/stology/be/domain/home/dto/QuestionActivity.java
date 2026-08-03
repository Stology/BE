package com.stology.be.domain.home.dto;


import com.stology.be.domain.home.enums.QuestionActivityType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QuestionActivity {

    private QuestionActivityType activityType;
    private boolean checked;

    private Long studyId;
    private Long questionId;
    private Long answerId;

    private String questionTitle;
    private String studyName;
    private String writerName;

    private LocalDateTime createdAt;

    public int getTypeRank() {
        return activityType.getRank();
    }

    public Long getCursorId() {
        return activityType == QuestionActivityType.QUESTION
                ? questionId
                : answerId;
    }
}