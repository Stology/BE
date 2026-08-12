package com.stology.be.domain.node.dto;


//UploadFilePromptBuilder 에서 시스템 프롬프트 만들떄 사용
public record StudyNodePromptDto(
        Long studyNodeId,
        String title
) {
}