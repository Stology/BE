package com.stology.be.domain.study.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class StudyReqDTO {

    // 스터디 방 생성
    public record CreateStudy(
            @NotBlank(message = "스터디 방 이름은 빈칸일 수 없습니다.")
            String name,
            @NotNull(message = "템플릿은 필수입니다.")
            Long templateId,
            @NotNull(message = "시작일은 필수입니다.")
            LocalDate startDate,
            String description
    ){}

    // 스터디 방 정보 수정
    public record UpdateStudy(
            @NotBlank(message = "스터디 방 이름은 빈칸일 수 없습니다.")
            String name,
            String description,
            @NotNull(message = "시작일은 필수입니다.")
            LocalDate startDate
    ){}

    // 검토 인원수 조정
    public record UpdateReviewerCount(
            @NotNull(message = "검토 인원수는은 필수입니다.")
            @Min(1)
            Integer reviewerCount
    ){}
}
