package com.stology.be.domain.template.dto;

import java.util.List;
import java.util.Map;

public record TemplateActivateDto(

    Long studyId,

    Long templateId,

    /*
     * TemplateNode ID
     *       ↓
     * MySQL StudyNode ID
     */
    Map<Long, Long> studyNodeIdMap,

    /*
     * 보상 삭제에 사용할 MySQL StudyNode ID 목록
     */
    List<Long> createdStudyNodeIds

) {
    }