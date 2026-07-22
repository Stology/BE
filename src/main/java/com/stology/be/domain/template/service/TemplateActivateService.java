package com.stology.be.domain.template.service;

import com.stology.be.domain.node.entity.neo4j.TemplateGraphNode;
import com.stology.be.domain.node.entity.neo4j.TemplateNodeGraphNode;
import com.stology.be.domain.node.repository.neo4j.TemplateGraphRepository;
import com.stology.be.domain.template.dto.TemplateActivateDto;
import com.stology.be.domain.template.dto.TemplateNodeActivateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateActivateService {

    private final TemplateGraphRepository templateGraphRepository;
    private final MysqlTemplateActivateService mysqlActivateService;
    private final Neo4jTemplateActivateService neo4jActivateService;

    public Long activateTemplate(
            Long studyId,
            Long templateId
    ) {
        validateInput(studyId, templateId);

        /*
         * Neo4j 원본 Template을 읽어서
         * MySQL StudyNode 생성에 필요한 정보만 추출한다.
         *
         * 읽기 작업이므로 아직 데이터를 생성하지 않는다.
         */
        TemplateGraphNode template =
                getTemplate(templateId);

        List<TemplateNodeActivateDto> templateNodes =
                convertTemplateNodes(template);

        /*
         * 1. MySQL StudyNode 생성 및 커밋
         */
        TemplateActivateDto activateResult =
                mysqlActivateService.createStudyNodes(
                        studyId,
                        templateId,
                        templateNodes
                );
        try {
            /*
             * 2. MySQL ID를 사용하여 Neo4j 그래프 생성
             */
            return neo4jActivateService.createStudyGraph(
                    activateResult
            );

        } catch (RuntimeException neo4jException) {

            try {
                /*
                 * 3. Neo4j 실패 시 이번에 생성한
                 *    MySQL StudyNode만 보상 삭제
                 */
                mysqlActivateService.deleteCreatedStudyNodes(
                        activateResult.createdStudyNodeIds()
                );

            } catch (RuntimeException compensationException) {

                neo4jException.addSuppressed(
                        compensationException
                );

                throw new IllegalStateException(
                        "Neo4j 그래프 생성과 MySQL StudyNode 보상 삭제가 모두 실패했습니다.",
                        neo4jException
                );
            }

            throw new IllegalStateException(
                    "Neo4j 그래프 생성에 실패하여 MySQL StudyNode를 보상 삭제했습니다.",
                    neo4jException
            );
        }
    }

    private void validateInput(
            Long studyId,
            Long templateId
    ) {
        if (studyId == null) {
            throw new IllegalArgumentException(
                    "studyId는 필수입니다."
            );
        }

        if (templateId == null) {
            throw new IllegalArgumentException(
                    "templateId는 필수입니다."
            );
        }
    }

    private TemplateGraphNode getTemplate(
            Long templateId
    ) {
        return templateGraphRepository.findById(templateId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 템플릿입니다. templateId="
                                        + templateId
                        )
                );
    }

    private List<TemplateNodeActivateDto> convertTemplateNodes(
            TemplateGraphNode template
    ) {
        return template.getTemplateNodes()
                .stream()
                .map(this::convertTemplateNode)
                .toList();
    }

    private TemplateNodeActivateDto convertTemplateNode(
            TemplateNodeGraphNode templateNode
    ) {
        if (templateNode.getTemplateNodeId() == null) {
            throw new IllegalStateException(
                    "TemplateNode ID가 없습니다. title="
                            + templateNode.getTitle()
            );
        }

        return new TemplateNodeActivateDto(
                templateNode.getTemplateNodeId(),
                templateNode.getTitle(),
                templateNode.getWeek()
        );
    }
}