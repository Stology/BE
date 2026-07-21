package com.stology.be.domain.template.service;

import com.stology.be.domain.template.dto.TemplateImportDto;
import com.stology.be.domain.template.dto.TemplateNodeDto;
import com.stology.be.domain.template.dto.TemplateRelationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemplateImportService {

    private final MysqlTemplateImportService mysqlImportService;
    private final Neo4jTemplateImportService neo4jImportService;

    public void importTemplate(TemplateImportDto dto) {

        validateTemplate(dto);

        Long templateId =
                mysqlImportService.saveTemplate(dto);

        try {
            neo4jImportService.saveTemplate(
                    templateId,
                    dto
            );
        } catch (RuntimeException neo4jException) {

            try {
                mysqlImportService.deleteTemplate(templateId);
            } catch (RuntimeException compensationException) {
                neo4jException.addSuppressed(
                        compensationException
                );

                throw new IllegalStateException(
                        "Neo4j 저장과 MySQL 보상 삭제가 모두 실패했습니다.",
                        neo4jException
                );
            }

            throw new IllegalStateException(
                    "Neo4j 저장에 실패하여 MySQL 저장을 취소했습니다.",
                    neo4jException
            );
        }
    }








    private void validateTemplate(TemplateImportDto dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "템플릿 데이터가 없습니다."
            );
        }

        if (dto.title() == null || dto.title().isBlank()) {
            throw new IllegalArgumentException(
                    "템플릿 제목은 필수입니다."
            );
        }

        if (dto.nodes() == null || dto.nodes().isEmpty()) {
            throw new IllegalArgumentException(
                    "템플릿에는 최소 하나의 노드가 필요합니다."
            );
        }

        if (dto.relations() == null) {
            throw new IllegalArgumentException(
                    "템플릿 관계 목록은 null일 수 없습니다."
            );
        }

        for (TemplateNodeDto node : dto.nodes()) {

            if (node == null) {
                throw new IllegalArgumentException(
                        "노드 정보는 null일 수 없습니다."
                );
            }

            if (node.title() == null || node.title().isBlank()) {
                throw new IllegalArgumentException(
                        "노드 제목은 필수입니다."
                );
            }

            if (node.week() <= 0) {
                throw new IllegalArgumentException(
                        "노드 주차는 1 이상이어야 합니다: "
                                + node.title()
                );
            }
        }

        for (TemplateRelationDto relation : dto.relations()) {

            if (relation == null) {
                throw new IllegalArgumentException(
                        "관계 정보는 null일 수 없습니다."
                );
            }

            if (relation.sourceTitle() == null
                    || relation.sourceTitle().isBlank()) {
                throw new IllegalArgumentException(
                        "관계의 sourceTitle은 필수입니다."
                );
            }

            if (relation.targetTitle() == null
                    || relation.targetTitle().isBlank()) {
                throw new IllegalArgumentException(
                        "관계의 targetTitle은 필수입니다."
                );
            }

            if (relation.relation() == null
                    || relation.relation().isBlank()) {
                throw new IllegalArgumentException(
                        "관계 설명은 필수입니다."
                );
            }
        }
    }


}