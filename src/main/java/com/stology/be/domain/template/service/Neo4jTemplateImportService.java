package com.stology.be.domain.template.service;

import com.stology.be.domain.node.entity.neo4j.TemplateGraphNode;
import com.stology.be.domain.node.entity.neo4j.TemplateNodeGraphNode;
import com.stology.be.domain.node.repository.neo4j.TemplateGraphRepository;
import com.stology.be.domain.template.dto.TemplateImportDto;
import com.stology.be.domain.template.dto.TemplateNodeDto;
import com.stology.be.domain.template.dto.TemplateRelationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class Neo4jTemplateImportService {
    private final TemplateGraphRepository templateGraphRepository;

    @Transactional("neo4jTransactionManager")
    public void saveTemplate(
            Long templateId,
            TemplateImportDto dto
    ) {

        TemplateGraphNode template =
                new TemplateGraphNode(
                        templateId
                );

        Map<String, TemplateNodeGraphNode> nodeMap =
                new HashMap<>();

        createTemplateNodes(
                dto,
                template,
                nodeMap
        );

        createTemplateRelations(
                dto,
                nodeMap
        );

        templateGraphRepository.save(template);
    }

    private void createTemplateNodes(
            TemplateImportDto dto,
            TemplateGraphNode template,
            Map<String, TemplateNodeGraphNode> nodeMap
    ) {
        for (TemplateNodeDto nodeDto : dto.nodes()) {

            String nodeTitle =
                    nodeDto.title().trim();

            if (nodeMap.containsKey(nodeTitle)) {
                throw new IllegalArgumentException(
                        "템플릿 안에 중복된 노드 제목이 있습니다: "
                                + nodeTitle
                );
            }

            TemplateNodeGraphNode graphNode =
                    new TemplateNodeGraphNode(nodeTitle,nodeDto.week());

            nodeMap.put(
                    nodeTitle,
                    graphNode
            );

            template.addTemplateNode(graphNode);
        }
    }

    private void createTemplateRelations(
            TemplateImportDto dto,
            Map<String, TemplateNodeGraphNode> nodeMap
    ) {
        for (TemplateRelationDto relationDto
                : dto.relations()) {

            String sourceTitle =
                    relationDto.sourceTitle().trim();

            String targetTitle =
                    relationDto.targetTitle().trim();

            TemplateNodeGraphNode sourceNode =
                    nodeMap.get(sourceTitle);

            TemplateNodeGraphNode targetNode =
                    nodeMap.get(targetTitle);

            if (sourceNode == null) {
                throw new IllegalArgumentException(
                        "관계의 출발 노드를 찾을 수 없습니다: "
                                + sourceTitle
                );
            }

            if (targetNode == null) {
                throw new IllegalArgumentException(
                        "관계의 도착 노드를 찾을 수 없습니다: "
                                + targetTitle
                );
            }

            sourceNode.addRelation(
                    relationDto.relation().trim(),
                    targetNode
            );
        }
    }
}