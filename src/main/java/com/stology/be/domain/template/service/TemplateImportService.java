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
public class TemplateImportService {
    private final TemplateGraphRepository templateGraphRepository;

    @Transactional("neo4jTransactionManager")
    public void importTemplate(TemplateImportDto dto) {

        validateTemplate(dto);

        /*
         * 같은 제목의 템플릿이 이미 존재하면
         * 서버가 시작될 때마다 중복으로 저장되는 것을 방지한다.
         */
        /*
        if (templateGraphRepository.existsByTitle(dto.title())) {
            return;
        }

         */

        // 1. Template 생성
        TemplateGraphNode template =
                new TemplateGraphNode(dto.title());

        /*
         * 관계 정보에서는 노드를 ID가 아니라 title로 찾기 때문에
         * title을 Key로 사용하는 Map을 생성한다.
         */
        Map<String, TemplateNodeGraphNode> nodeMap =
                new HashMap<>();

        // 2. TemplateNode 생성
        for (TemplateNodeDto nodeDto : dto.nodes()) {

            String nodeTitle = nodeDto.title().trim();

            if (nodeMap.containsKey(nodeTitle)) {
                throw new IllegalArgumentException(
                        "템플릿 안에 중복된 노드 제목이 있습니다: "
                                + nodeTitle
                );
            }

            TemplateNodeGraphNode graphNode =
                    new TemplateNodeGraphNode(nodeTitle);

            nodeMap.put(nodeTitle, graphNode);

            template.addTemplateNode(graphNode);
        }

        // 3. 노드 사이의 관계 연결
        for (TemplateRelationDto relationDto : dto.relations()) {

            TemplateNodeGraphNode sourceNode =
                    nodeMap.get(relationDto.sourceTitle());

            TemplateNodeGraphNode targetNode =
                    nodeMap.get(relationDto.targetTitle());

            if (sourceNode == null) {
                throw new IllegalArgumentException(
                        "관계의 출발 노드를 찾을 수 없습니다: "
                                + relationDto.sourceTitle()
                );
            }

            if (targetNode == null) {
                throw new IllegalArgumentException(
                        "관계의 도착 노드를 찾을 수 없습니다: "
                                + relationDto.targetTitle()
                );
            }

            sourceNode.addRelation(
                    relationDto.relation(),
                    targetNode
            );
        }

        /*
         * Template을 루트 엔티티로 저장한다.
         *
         * Template
         *   └─ HAS_NODE → TemplateNode
         *                      └─ RELATED_TO → TemplateNode
         *
         * 연결된 노드와 관계도 함께 저장된다.
         */
        templateGraphRepository.save(template);
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
                    "relations는 null일 수 없습니다."
            );
        }

        for (TemplateNodeDto node : dto.nodes()) {
            if (node.title() == null || node.title().isBlank()) {
                throw new IllegalArgumentException(
                        "노드 제목은 필수입니다."
                );
            }
        }

        for (TemplateRelationDto relation : dto.relations()) {

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