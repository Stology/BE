package com.stology.be.domain.template.service;

import com.stology.be.domain.node.entity.neo4j.TemplateGraphNode;
import com.stology.be.domain.node.entity.neo4j.TemplateNodeGraphNode;
import com.stology.be.domain.node.entity.neo4j.TemplateNodeRelation;
import com.stology.be.domain.node.entity.neo4j.copy.StudyNodeGraphNode;
import com.stology.be.domain.node.entity.neo4j.copy.TemplateStudyGraphNode;
import com.stology.be.domain.node.repository.neo4j.TemplateGraphRepository;
import com.stology.be.domain.node.repository.neo4j.copy.TemplateStudyGraphRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TemplateActivateService {

    private final TemplateGraphRepository templateGraphRepository;
    private final TemplateStudyGraphRepository templateStudyGraphRepository;

    @Transactional("neo4jTransactionManager")
    public Long makeDuplicate(
            Long studyId,
            Long templateId
    ) {
        TemplateGraphNode template =
                getTemplate(templateId);

        validateDuplicateStudy(studyId);

        TemplateStudyGraphNode templateStudy =
                createTemplateStudy(
                        studyId,
                        templateId,
                        template
                );

        Map<Long, StudyNodeGraphNode> copiedNodeMap =
                copyTemplateNodes(
                        template,
                        templateStudy
                );

        copyTemplateRelations(
                template,
                copiedNodeMap
        );
        TemplateStudyGraphNode savedTemplateStudy =
                saveTemplateStudy(templateStudy);


        return savedTemplateStudy.getTemplateStudyId();
    }







    /**
     * templateId로 원본 Template 그래프를 조회한다.
     */
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

    /**
     * 같은 Study에 TemplateStudy가 중복으로 생성되는 것을 방지한다.
     */
    private void validateDuplicateStudy(
            Long studyId
    ) {
        boolean alreadyExists =
                templateStudyGraphRepository.existsByStudyId(studyId);

        if (alreadyExists) {
            throw new IllegalStateException(
                    "이미 그래프가 생성된 스터디입니다. studyId="
                            + studyId
            );
        }
    }

    /**
     * Template 정보와 studyId를 이용해
     * 새로운 TemplateStudy 루트 노드를 생성한다.
     */
    private TemplateStudyGraphNode createTemplateStudy(
            Long studyId,
            Long templateId,
            TemplateGraphNode template
    ) {
        return new TemplateStudyGraphNode(
                studyId,
                templateId
        );
    }

    /**
     * 모든 TemplateNode를 StudyNode로 복제한다.
     *
     * 반환되는 Map의 구조:
     *
     * 원본 TemplateNode ID
     *          ↓
     * 복제된 StudyNode 객체
     *
     * 이 Map은 이후 관계를 복제할 때 사용된다.
     */
    private Map<Long, StudyNodeGraphNode> copyTemplateNodes(
            TemplateGraphNode template,
            TemplateStudyGraphNode templateStudy
    ) {
        Map<Long, StudyNodeGraphNode> copiedNodeMap =
                new HashMap<>();

        for (TemplateNodeGraphNode templateNode
                : template.getTemplateNodes()) {

            StudyNodeGraphNode studyNode =
                    createStudyNode(templateNode);

            addCopiedNodeToMap(
                    copiedNodeMap,
                    templateNode,
                    studyNode
            );

            templateStudy.addStudyNode(studyNode);
        }

        return copiedNodeMap;
    }

    /**
     * TemplateNode 하나를 StudyNode로 복제한다.
     */
    private StudyNodeGraphNode createStudyNode(
            TemplateNodeGraphNode templateNode
    ) {
        return new StudyNodeGraphNode(
                templateNode.getTitle()
        );
    }

    /**
     * 원본 TemplateNode와 복제 StudyNode의 대응 관계를 Map에 저장한다.
     */
    private void addCopiedNodeToMap(
            Map<Long, StudyNodeGraphNode> copiedNodeMap,
            TemplateNodeGraphNode templateNode,
            StudyNodeGraphNode studyNode
    ) {
        Long templateNodeId =
                templateNode.getTemplateNodeId();

        if (templateNodeId == null) {
            throw new IllegalStateException(
                    "원본 TemplateNode의 ID가 없습니다. title="
                            + templateNode.getTitle()
            );
        }

        copiedNodeMap.put(
                templateNodeId,
                studyNode
        );
    }

    /**
     * 원본 TemplateNode 사이의 모든 관계를
     * 복제된 StudyNode 사이에 그대로 생성한다.
     */
    private void copyTemplateRelations(
            TemplateGraphNode template,
            Map<Long, StudyNodeGraphNode> copiedNodeMap
    ) {
        for (TemplateNodeGraphNode sourceTemplateNode
                : template.getTemplateNodes()) {

            copyRelationsOfNode(
                    sourceTemplateNode,
                    copiedNodeMap
            );
        }
    }

    /**
     * 특정 TemplateNode가 가진 모든 outgoing 관계를 복제한다.
     */
    private void copyRelationsOfNode(
            TemplateNodeGraphNode sourceTemplateNode,
            Map<Long, StudyNodeGraphNode> copiedNodeMap
    ) {
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

    /**
     * TemplateNodeRelation 하나를 StudyNodeRelation으로 복제한다.
     */
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

    /**
     * 원본 TemplateNode에 대응하는 복제 StudyNode를 Map에서 조회한다.
     */
    private StudyNodeGraphNode getCopiedStudyNode(
            TemplateNodeGraphNode templateNode,
            Map<Long, StudyNodeGraphNode> copiedNodeMap,
            String nodeRole
    ) {
        Long templateNodeId =
                templateNode.getTemplateNodeId();

        StudyNodeGraphNode studyNode =
                copiedNodeMap.get(templateNodeId);

        if (studyNode == null) {
            throw new IllegalStateException(
                    "복제된 " + nodeRole
                            + " 노드를 찾을 수 없습니다. templateNodeId="
                            + templateNodeId
            );
        }

        return studyNode;
    }

    /**
     * 완성된 TemplateStudy 객체 그래프를 저장한다.
     */
    private TemplateStudyGraphNode saveTemplateStudy(
            TemplateStudyGraphNode templateStudy
    ) {
        return templateStudyGraphRepository.save(templateStudy);
    }
}