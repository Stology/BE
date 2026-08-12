package com.stology.be.domain.template.service;

import com.stology.be.domain.node.entity.neo4j.TemplateGraphNode;
import com.stology.be.domain.node.entity.neo4j.TemplateNodeGraphNode;
import com.stology.be.domain.node.entity.neo4j.TemplateNodeRelation;
import com.stology.be.domain.node.entity.neo4j.copy.StudyNodeGraphNode;
import com.stology.be.domain.node.entity.neo4j.copy.TemplateStudyGraphNode;
import com.stology.be.domain.node.repository.neo4j.TemplateGraphRepository;
import com.stology.be.domain.node.repository.neo4j.copy.TemplateStudyGraphRepository;

import com.stology.be.domain.template.dto.TemplateActivateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class Neo4jTemplateActivateService {

    private final TemplateGraphRepository
            templateGraphRepository;

    private final TemplateStudyGraphRepository
            templateStudyGraphRepository;

    @Transactional("neo4jTransactionManager")
    public Long createStudyGraph(
            TemplateActivateDto result
    ) {
        validateDuplicateStudy(result.studyId());

        TemplateGraphNode template =
                getTemplate(result.templateId());

        TemplateStudyGraphNode templateStudy =
                new TemplateStudyGraphNode(
                        result.studyId(),
                        result.templateId()
                );

        /*
         * TemplateNode ID
         *       ↓
         * 새로 생성한 StudyNodeGraphNode 객체
         */
        Map<Long, StudyNodeGraphNode> copiedNodeMap =
                copyTemplateNodes(
                        template,
                        templateStudy,
                        result.studyNodeIdMap()
                );

        copyTemplateRelations(
                template,
                copiedNodeMap
        );

        TemplateStudyGraphNode savedTemplateStudy =
                templateStudyGraphRepository.save(
                        templateStudy
                );

        return savedTemplateStudy.getTemplateStudyId();
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

    private void validateDuplicateStudy(
            Long studyId
    ) {
        if (templateStudyGraphRepository
                .existsByStudyId(studyId)) {

            throw new IllegalStateException(
                    "이미 그래프가 생성된 스터디입니다. studyId="
                            + studyId
            );
        }
    }

    private Map<Long, StudyNodeGraphNode>
    copyTemplateNodes(
            TemplateGraphNode template,
            TemplateStudyGraphNode templateStudy,
            Map<Long, Long> studyNodeIdMap
    ) {
        Map<Long, StudyNodeGraphNode> copiedNodeMap =
                new HashMap<>();

        for (TemplateNodeGraphNode templateNode
                : template.getTemplateNodes()) {

            Long templateNodeId =
                    getTemplateNodeId(templateNode);

            Long studyNodeId =
                    studyNodeIdMap.get(templateNodeId);

            if (studyNodeId == null) {
                throw new IllegalStateException(
                        "TemplateNode에 대응하는 MySQL StudyNode ID가 없습니다. "
                                + "templateNodeId="
                                + templateNodeId
                );
            }

            StudyNodeGraphNode studyNode =
                    new StudyNodeGraphNode(
                            studyNodeId,
                            templateNode.getTitle(),
                            templateNode.getWeek()
                    );

            StudyNodeGraphNode previousNode =
                    copiedNodeMap.put(
                            templateNodeId,
                            studyNode
                    );

            if (previousNode != null) {
                throw new IllegalStateException(
                        "중복된 TemplateNode ID입니다. templateNodeId="
                                + templateNodeId
                );
            }

            templateStudy.addStudyNode(studyNode);
        }

        //성공적으로 카피 되었는지 확인
        validateCopiedNodeCount(
                template,
                studyNodeIdMap,
                copiedNodeMap
        );

        return copiedNodeMap;
    }

    private void validateCopiedNodeCount(
            TemplateGraphNode template,
            Map<Long, Long> studyNodeIdMap,
            Map<Long, StudyNodeGraphNode> copiedNodeMap
    ) {
        int templateNodeCount =
                template.getTemplateNodes().size();

        if (studyNodeIdMap.size() != templateNodeCount
                || copiedNodeMap.size() != templateNodeCount) {

            throw new IllegalStateException(
                    "TemplateNode와 StudyNode의 개수가 일치하지 않습니다. "
                            + "templateNodes="
                            + templateNodeCount
                            + ", mysqlStudyNodes="
                            + studyNodeIdMap.size()
                            + ", graphStudyNodes="
                            + copiedNodeMap.size()
            );
        }
    }

    private void copyTemplateRelations(
            TemplateGraphNode template,
            Map<Long, StudyNodeGraphNode> copiedNodeMap
    ) {
        for (TemplateNodeGraphNode sourceTemplateNode
                : template.getTemplateNodes()) {

            StudyNodeGraphNode sourceStudyNode =
                    getCopiedStudyNode(
                            sourceTemplateNode,
                            copiedNodeMap,
                            "출발"
                    );

            for (TemplateNodeRelation templateRelation
                    : sourceTemplateNode.getRelatedNodes()) {

                copySingleRelation(
                        sourceStudyNode,
                        templateRelation,
                        copiedNodeMap
                );
            }
        }
    }

    private void copySingleRelation(
            StudyNodeGraphNode sourceStudyNode,
            TemplateNodeRelation templateRelation,
            Map<Long, StudyNodeGraphNode> copiedNodeMap
    ) {
        TemplateNodeGraphNode targetTemplateNode =
                templateRelation.getTargetNode();

        if (targetTemplateNode == null) {
            throw new IllegalStateException(
                    "TemplateNodeRelation의 targetNode가 없습니다. relation="
                            + templateRelation.getRelation()
            );
        }

        StudyNodeGraphNode targetStudyNode =
                getCopiedStudyNode(
                        targetTemplateNode,
                        copiedNodeMap,
                        "도착"
                );

        sourceStudyNode.addRelatedNode(
                templateRelation.getRelation(),
                targetStudyNode
        );
    }

    private StudyNodeGraphNode getCopiedStudyNode(
            TemplateNodeGraphNode templateNode,
            Map<Long, StudyNodeGraphNode> copiedNodeMap,
            String nodeRole
    ) {
        Long templateNodeId =
                getTemplateNodeId(templateNode);

        StudyNodeGraphNode studyNode =
                copiedNodeMap.get(templateNodeId);

        if (studyNode == null) {
            throw new IllegalStateException(
                    "복제된 " + nodeRole
                            + " StudyNode를 찾을 수 없습니다. templateNodeId="
                            + templateNodeId
            );
        }

        return studyNode;
    }

    private Long getTemplateNodeId(
            TemplateNodeGraphNode templateNode
    ) {
        Long templateNodeId =
                templateNode.getTemplateNodeId();

        if (templateNodeId == null) {
            throw new IllegalStateException(
                    "TemplateNode ID가 없습니다. title="
                            + templateNode.getTitle()
            );
        }

        return templateNodeId;
    }
}