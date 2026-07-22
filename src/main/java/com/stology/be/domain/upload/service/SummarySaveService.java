package com.stology.be.domain.upload.service;

import com.stology.be.domain.node.entity.NodeCandidate;
import com.stology.be.domain.node.entity.StudyMaterial;
import com.stology.be.domain.node.entity.StudyNode;
import com.stology.be.domain.node.enums.CandidateState;
import com.stology.be.domain.node.repository.NodeCandidateRepository;
import com.stology.be.domain.node.repository.StudyMaterialRepository;
import com.stology.be.domain.node.repository.StudyNodeRepository;
import com.stology.be.domain.upload.enums.DataState;
import com.stology.be.global.external.ai.dto.AiSummaryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SummarySaveService {

    private final StudyMaterialRepository studyMaterialRepository;
    private final StudyNodeRepository studyNodeRepository;
    private final NodeCandidateRepository nodeCandidateRepository;

    @Transactional
    public void saveResult(
            Long studyMaterialId,
            AiSummaryResult result
    ) {
        validateResult(result);

        // 1. 업로드 자료 조회
        StudyMaterial studyMaterial =
                studyMaterialRepository.findById(studyMaterialId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 스터디 자료입니다. "
                                                + "studyMaterialId="
                                                + studyMaterialId
                                )
                        );

        // 2. AI 요약 저장
        studyMaterial.updateSummary(
                result.summary().trim()
        );


        // 4. 중복 노드 ID 제거
        Set<Long> studyNodeIds = new LinkedHashSet<>();

        for (AiSummaryResult.KeywordInfo node : result.keywords()) {
            if (node != null && node.Id() != null) {
                studyNodeIds.add(node.Id());
            }
        }


        // 프롬프트 규칙상 최대 3개
        if (studyNodeIds.size() > 3) {
            throw new IllegalArgumentException(
                    "AI가 선택할 수 있는 노드는 최대 3개입니다."
            );
        }

        // 5. StudyNode 일괄 조회
        List<StudyNode> studyNodes =
                studyNodeRepository.findAllById(studyNodeIds);

        validateStudyNodes(
                studyMaterial,
                studyNodeIds,
                studyNodes
        );

        // 노드 후보 만들기
        List<NodeCandidate> candidates =
                studyNodes.stream()
                        .map(studyNode ->
                                NodeCandidate
                                        .builder()
                                        .studyMaterial(studyMaterial)
                                        .studyNode(studyNode)
                                        .state(CandidateState.PENDING)
                                        .build()
                        )
                        .toList();

        // 8. 후보 일괄 저장
        nodeCandidateRepository.saveAll(candidates);

    }

    private void validateResult(AiSummaryResult result) {
        if (result == null) {
            throw new IllegalArgumentException(
                    "AI 응답 결과가 없습니다."
            );
        }

        if (result.summary() == null
                || result.summary().isBlank()) {
            throw new IllegalArgumentException(
                    "AI 요약 결과가 비어 있습니다."
            );
        }
    }

    private void validateStudyNodes(
            StudyMaterial studyMaterial,
            Set<Long> requestedNodeIds,
            List<StudyNode> studyNodes
    ) {
        // 요청한 개수와 조회한 개수가 다르면 존재하지 않는 ID가 포함됨
        if (requestedNodeIds.size() != studyNodes.size()) {
            Set<Long> foundIds = studyNodes.stream()
                    .map(StudyNode::getId)
                    .collect(java.util.stream.Collectors.toSet());

            Set<Long> missingIds =
                    new LinkedHashSet<>(requestedNodeIds);

            missingIds.removeAll(foundIds);

            throw new IllegalArgumentException(
                    "존재하지 않는 스터디 노드가 포함되어 있습니다. "
                            + "studyNodeIds=" + missingIds
            );
        }

        Long materialStudyId =
                studyMaterial.getMemberStudy()
                        .getStudy()
                        .getId();

        boolean hasDifferentStudyNode =
                studyNodes.stream()
                        .anyMatch(studyNode ->
                                !studyNode.getStudy()
                                        .getId()
                                        .equals(materialStudyId)
                        );

        if (hasDifferentStudyNode) {
            throw new IllegalArgumentException(
                    "업로드 자료와 다른 스터디의 노드를 "
                            + "후보로 등록할 수 없습니다."
            );
        }
    }

    private Integer resolveWeek(
            StudyMaterial studyMaterial
    ) {
        /*
         * StudyMaterial이나 MemberStudy에서 현재 주차를
         * 관리한다면 실제 필드로 교체.
         */
        return null;
    }
}