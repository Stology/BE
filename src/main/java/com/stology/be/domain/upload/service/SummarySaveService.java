package com.stology.be.domain.upload.service;

import com.stology.be.domain.node.entity.NodeCandidate;
import com.stology.be.domain.node.entity.StudyMaterial;
import com.stology.be.domain.node.entity.StudyNode;
import com.stology.be.domain.node.enums.CandidateState;
import com.stology.be.domain.node.repository.NodeCandidateRepository;
import com.stology.be.domain.node.repository.StudyMaterialRepository;
import com.stology.be.domain.node.repository.StudyNodeRepository;
import com.stology.be.domain.upload.exception.UploadException;
import com.stology.be.domain.upload.exception.code.UploadErrorCode;
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

    private final String NORELATION= " [올리신 자료가 스터디의 어떠한 노드와도 연관이 없습니다.]";

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
                                new UploadException(UploadErrorCode.STUDY_MATERIAL_NOT_FOUND)
                        );

        // 2. AI 요약 저장
        studyMaterial.updateSummary(
                result.summary().trim()
        );
        if (result.keywords().isEmpty()) {
            studyMaterial.updateSummary(studyMaterial.getSummary()+ NORELATION);
        }


        // 4. 중복 노드 ID 제거
        Set<Long> studyNodeIds = new LinkedHashSet<>();

        for (AiSummaryResult.KeywordInfo node : result.keywords()) {
            if (node != null && node.Id() != null) {
                studyNodeIds.add(node.Id());
            }
        }


        // 프롬프트 규칙상 최대 3개
        if (studyNodeIds.size() > 3) {
            throw new UploadException(UploadErrorCode.AI_SELECTED_NODE_LIMIT_EXCEEDED);

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
            throw new UploadException(UploadErrorCode.AI_RESULT_NOT_FOUND);
        }

        if (result.summary() == null
                || result.summary().isBlank()) {
            throw new UploadException(UploadErrorCode.AI_SUMMARY_EMPTY);
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

            throw new UploadException(UploadErrorCode.STUDY_NODE_NOT_FOUND);
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
            throw new UploadException(UploadErrorCode.STUDY_NODE_MISMATCH);
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