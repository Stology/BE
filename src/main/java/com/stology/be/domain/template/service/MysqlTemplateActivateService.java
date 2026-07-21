package com.stology.be.domain.template.service;

import com.stology.be.domain.node.entity.StudyNode;
import com.stology.be.domain.node.repository.StudyNodeRepository;
import com.stology.be.domain.study.entity.Study;
import com.stology.be.domain.study.repository.StudyRepository;
import com.stology.be.domain.template.dto.TemplateActivateDto;
import com.stology.be.domain.template.dto.TemplateNodeActivateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MysqlTemplateActivateService {

    private final StudyRepository studyRepository;
    private final StudyNodeRepository studyNodeRepository;

    @Transactional(transactionManager = "transactionManager")
    public TemplateActivateDto createStudyNodes(
            Long studyId,
            Long templateId,
            List<TemplateNodeActivateDto> templateNodes
    ) {
        Study study = getStudy(studyId);

        validateStudyNodesNotCreated(studyId);

        List<StudyNode> studyNodes =
                createStudyNodeEntities(
                        study,
                        templateNodes
                );

        /*
         * saveAll만 호출해도 트랜잭션 종료 시 반영되지만,
         * 생성된 ID를 즉시 사용해야 하므로 saveAllAndFlush를 사용한다.
         */
        List<StudyNode> savedStudyNodes =
                studyNodeRepository.saveAllAndFlush(studyNodes);

        Map<Long, Long> studyNodeIdMap =
                createIdMap(
                        templateNodes,
                        savedStudyNodes
                );

        List<Long> createdStudyNodeIds =
                savedStudyNodes.stream()
                        .map(StudyNode::getId)
                        .toList();

        return new TemplateActivateDto(
                studyId,
                templateId,
                studyNodeIdMap,
                createdStudyNodeIds
        );
    }

    @Transactional(
            transactionManager = "transactionManager",
            propagation = Propagation.REQUIRES_NEW
    )
    public void deleteCreatedStudyNodes(
            List<Long> studyNodeIds
    ) {
        if (studyNodeIds == null || studyNodeIds.isEmpty()) {
            return;
        }

        long deletedCount =
                studyNodeRepository.deleteByIdIn(
                        studyNodeIds
                );

        if (deletedCount != studyNodeIds.size()) {
            throw new IllegalStateException(
                    "MySQL 보상 삭제 개수가 일치하지 않습니다. expected="
                            + studyNodeIds.size()
                            + ", actual="
                            + deletedCount
            );
        }
    }








    private Study getStudy(
            Long studyId
    ) {
        return studyRepository.findById(studyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 스터디입니다. studyId="
                                        + studyId
                        )
                );
    }

    private void validateStudyNodesNotCreated(
            Long studyId
    ) {
        if (studyNodeRepository.existsByStudyId(studyId)) {
            throw new IllegalStateException(
                    "이미 복제가 되어있습니다."
                            + studyId
            );
        }
    }

    private List<StudyNode> createStudyNodeEntities(
            Study study,
            List<TemplateNodeActivateDto> templateNodes
    ) {
        List<StudyNode> studyNodes =
                new ArrayList<>();

        for (TemplateNodeActivateDto templateNode
                : templateNodes) {

            StudyNode studyNode =
                    StudyNode.createFromTemplate(
                            study,
                            templateNode.title(),
                            templateNode.week()
                    );

            studyNodes.add(studyNode);
        }

        return studyNodes;
    }

    private Map<Long, Long> createIdMap(
            List<TemplateNodeActivateDto> templateNodes,
            List<StudyNode> savedStudyNodes
    ) {
        if (templateNodes.size() != savedStudyNodes.size()) {
            throw new IllegalStateException(
                    "템플릿 노드와 생성된 StudyNode의 개수가 일치하지 않습니다."
            );
        }

        Map<Long, Long> studyNodeIdMap =
                new LinkedHashMap<>();

        for (int index = 0;
             index < templateNodes.size();
             index++) {

            Long templateNodeId =
                    templateNodes.get(index)
                            .templateNodeId();

            Long studyNodeId =
                    savedStudyNodes.get(index)
                            .getId();

            Long previousStudyNodeId =
                    studyNodeIdMap.put(
                            templateNodeId,
                            studyNodeId
                    );

            if (previousStudyNodeId != null) {
                throw new IllegalStateException(
                        "중복된 TemplateNode ID입니다. templateNodeId="
                                + templateNodeId
                );
            }
        }

        return studyNodeIdMap;
    }


}