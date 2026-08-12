package com.stology.be.domain.template.dto;

import java.util.List;

public record TemplateImportDto(

        String title,

        String description,

        List<TemplateNodeDto> nodes,

        List<TemplateRelationDto> relations

) {
}