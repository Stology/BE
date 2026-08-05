package com.stology.be.domain.node.service;

import com.stology.be.domain.node.dto.res.KnowledgeGraphResponseDto.*;
import com.stology.be.domain.node.entity.NodeCandidate;
import com.stology.be.domain.node.entity.StudyMaterial;
import com.stology.be.domain.node.entity.StudyNode;
import com.stology.be.domain.node.entity.neo4j.copy.StudyNodeGraphNode;
import com.stology.be.domain.node.entity.neo4j.copy.StudyNodeRelation;
import com.stology.be.domain.node.enums.CandidateState;
import com.stology.be.domain.node.repository.NodeCandidateRepository;
import com.stology.be.domain.node.repository.StudyNodeRepository;
import com.stology.be.domain.node.repository.neo4j.copy.StudyNodeGraphRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeGraphService {

    private final StudyNodeRepository studyNodeRepository;
    private final StudyNodeGraphRepository studyNodeGraphRepository;
    private final NodeCandidateRepository nodeCandidateRepository;

    public GraphRes getKnowledgeGraph(Long studyId) {
        // 1. RDB에서 StudyNode 기본 정보 조회
        List<StudyNode> studyNodes = studyNodeRepository.findByStudy_Id(studyId);

        List<NodeDto> nodeDtos = studyNodes.stream()
                .map(node -> NodeDto.builder()
                        .id(node.getId())
                        .title(node.getTitle())
                        .description(node.getDescription())
                        .activeLevel(node.getActiveLevel())
                        .activationWeek(node.getActivationWeek())
                        .recommendWeek(node.getRecommendWeek())
                        .build())
                .collect(Collectors.toList());

        // 2. Neo4j에서 관계 정보 조회 (RELATED_TO 관계 포함)
        List<StudyNodeGraphNode> graphNodes = studyNodeGraphRepository.findAllWithRelationsByStudyId(studyId);

        List<EdgeDto> edgeDtos = new ArrayList<>();
        for (StudyNodeGraphNode graphNode : graphNodes) {
            if (graphNode.getRelatedNodes() != null) {
                for (StudyNodeRelation relation : graphNode.getRelatedNodes()) {
                    if (relation.getTargetNode() != null) {
                        edgeDtos.add(EdgeDto.builder()
                                .source(graphNode.getStudyNodeId())
                                .target(relation.getTargetNode().getStudyNodeId())
                                .relation(relation.getRelation())
                                .build());
                    }
                }
            }
        }

        return GraphRes.builder()
                .nodes(nodeDtos)
                .edges(edgeDtos)
                .build();
    }

    public NodeDetailRes getNodeDetail(Long studyId, Long nodeId) {
        // 1. RDB에서 노드 정보 조회 (studyId 검증 포함 - 다른 스터디 노드 접근 방지)
        StudyNode studyNode = studyNodeRepository.findByIdAndStudyId(nodeId, studyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 노드입니다. nodeId=" + nodeId + ", studyId=" + studyId));

        // 2. 연관된 StudyMaterial 조회 (ACCEPTED 상태인 것 위주 - Fetch Join 쿼리 활용)
        List<NodeCandidate> candidates = nodeCandidateRepository.findAcceptedCandidatesWithMaterial(studyId, nodeId, CandidateState.ACCEPTED);

        // 자료 최신순 정렬
        List<StudyMaterial> materials = candidates.stream()
                .map(NodeCandidate::getStudyMaterial)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(StudyMaterial::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        // 노드 정의 (1순위: StudyNode description, 2순위 fallback: 첫 번째 자료 summary/content)
        String definition = studyNode.getDescription();
        if ((definition == null || definition.trim().isEmpty()) && !materials.isEmpty()) {
            StudyMaterial firstMaterial = materials.get(0);
            definition = (firstMaterial.getSummary() != null && !firstMaterial.getSummary().trim().isEmpty())
                    ? firstMaterial.getSummary()
                    : firstMaterial.getContent();
        }

        List<MaterialDto> recentMaterials = materials.stream()
                .limit(5)
                .map(m -> MaterialDto.builder()
                        .id(m.getId())
                        .title(m.getDataTitle() != null ? m.getDataTitle() : m.getObjectKey())
                        .memberName(m.getMemberStudy() != null && m.getMemberStudy().getMember() != null
                                ? m.getMemberStudy().getMember().getName()
                                : "알 수 없음")
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // 3. Neo4j에서 연결된 관계 그룹화
        Map<String, List<ConnectedNodeDto>> relationGroups = new LinkedHashMap<>();
        Optional<StudyNodeGraphNode> graphNodeOpt = studyNodeGraphRepository.findWithRelationsByStudyIdAndStudyNodeId(studyId, nodeId);

        if (graphNodeOpt.isPresent() && graphNodeOpt.get().getRelatedNodes() != null) {
            List<StudyNode> studyNodes = studyNodeRepository.findByStudy_Id(studyId);
            Map<Long, StudyNode> nodeMap = studyNodes.stream()
                    .collect(Collectors.toMap(StudyNode::getId, n -> n, (existing, replacement) -> existing));

            for (StudyNodeRelation relation : graphNodeOpt.get().getRelatedNodes()) {
                if (relation.getTargetNode() != null) {
                    String relationType = relation.getRelation();
                    StudyNodeGraphNode targetGraph = relation.getTargetNode();
                    Long targetId = targetGraph.getStudyNodeId();
                    StudyNode targetRdbNode = nodeMap.get(targetId);

                    int targetActiveLevel = (targetRdbNode != null && targetRdbNode.getActiveLevel() != null)
                            ? targetRdbNode.getActiveLevel()
                            : 0;

                    String targetTitle = targetGraph.getTitle() != null
                            ? targetGraph.getTitle()
                            : (targetRdbNode != null ? targetRdbNode.getTitle() : "");

                    ConnectedNodeDto connected = ConnectedNodeDto.builder()
                            .nodeId(targetId)
                            .title(targetTitle)
                            .activeLevel(targetActiveLevel)
                            .build();

                    relationGroups.computeIfAbsent(relationType, k -> new ArrayList<>()).add(connected);
                }
            }
        }

        boolean isActive = studyNode.getActiveLevel() != null && studyNode.getActiveLevel() > 0;

        return NodeDetailRes.builder()
                .nodeId(nodeId)
                .title(studyNode.getTitle())
                .definition(definition)
                .activeLevel(studyNode.getActiveLevel())
                .isActive(isActive)
                .materialCount(materials.size())
                .recentMaterials(recentMaterials)
                .relations(relationGroups)
                .build();
    }
}
