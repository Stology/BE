package com.stology.be.domain.study.dto;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

public class StudyResDTO {

    // 참여한 스터디 방 목록 조회
    public record GetStudy(
            List<Study> studies
    ){}
    public record Study(
            Long studyId,
            String name,
            LocalDateTime startDate,
            Boolean isNew
    ){}

    // 온톨로지 템플릿 검색
    public record GetTemplate(
            List<Template> templates
    ){}
    public record Template(
            Long templateId,
            String title,
            String uploader,
            String description
    ){}

    // 검토 인원수 조회
    @Builder
    public record GetReviewerCount(
            Integer reviewerCount,
            Integer maxReviewerCount
    ){}

    // 초대 토큰 조회 -> 스터디방 정보 반환
    @Builder
    public record  GetInvitationToken(
            Long studyId,
            String name,
            String leader,
            Integer memberCount
    ){}

    // 스터디 종료
    @Builder
    public record CloseStudy(
            Integer activeNodeCount,
            Integer uploadedMaterialCount,
            Integer questionCount
    ){}

    // 스터디 단일 조회
    @Builder
    public record GetStudyDetail(
            Long studyId,
            String name,
            Integer currentWeek,  // 스터디 시작일 기준 현재 몇 주차인지
            Boolean isActive,
            Boolean isLeader
    ){}
}

