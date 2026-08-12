package com.stology.be.domain.home.dto.res;

import com.stology.be.global.PageInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class QuestionDetailRes {

    private PageInfo<Long> pageInfo;
    private List<QuestionInfo> questions;

    @Getter
    @Builder
    public static class QuestionInfo {

        private boolean checked;
        private Long studyId;
        private Long questionId;
        private String questionTitle;
        private String studyName;
        private String writerName;
        private LocalDateTime createdAt;
    }
}