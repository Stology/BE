package com.stology.be.domain.study.dto;

import lombok.Builder;
import java.time.LocalDate;
import java.util.List;

public class StudyResDTO {

    // 참여한 스터디 방 목록 조회
    public record GetStudy(
            List<Study> studies
    ){}
    public record Study(
            Long studyId,
            String name,
            LocalDate startDate,
            String description,
            Boolean isActive
    ){}

    // 온톨로지 템플릿 검색
    public record GetTemplate(
            List<Template> templates
    ){}
    public record Template(
            Long templateId,
            String name,
            String uploader,
            String description
    ){}

    // 검토 인원수 조회
    @Builder
    public record GetReviewerCount(
            Integer reviewerCount,
            Integer maxReviewerCount
    ){}
}

