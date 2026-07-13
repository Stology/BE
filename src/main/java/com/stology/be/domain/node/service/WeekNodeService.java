package com.stology.be.domain.node.service;

import com.stology.be.domain.node.dto.res.NodeInfoRes;
import com.stology.be.domain.node.dto.res.WeekNodeRes;
import com.stology.be.domain.node.entity.NodeCandidate;
import com.stology.be.domain.node.entity.StudyMaterial;
import com.stology.be.domain.node.entity.StudyNode;
import com.stology.be.domain.node.enums.CandidateState;
import com.stology.be.domain.node.repository.NodeCandidateRepository;
import com.stology.be.domain.node.repository.StudyNodeRepository;
import com.stology.be.domain.study.repository.MemberStudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WeekNodeService {

    private final StudyNodeRepository studyNodeRepository;
    private final MemberStudyRepository memberStudyRepository;
    private final NodeCandidateRepository nodeCandidateRepository;


    private static final int MIN_ACTIVE_LEVEL = 1;
    private static final int MAX_ACTIVE_LEVEL = 10;


    public WeekNodeRes getWeekNodes(Long studyId, int week, Long authMemberId) {

        List<WeekNodeRes.WeekNodeInfo> nodes =
                studyNodeRepository
                        .findByStudy_IdAndActivationWeekAndActiveLevelBetweenOrderByActiveLevelAsc(
                                studyId,
                                week,
                                1,
                                10
                        )
                        .stream()
                        .map(WeekNodeRes.WeekNodeInfo::from)
                        .toList();

        return new WeekNodeRes(nodes);
    }

    public NodeInfoRes getNodeInfo(
            Long studyId,
            Long nodeId,
            Long memberId
    ) {

        // 1. 요청한 사용자가 해당 스터디의 멤버인지 확인
        validateStudyMember(studyId, memberId);

        // 2. 요청한 노드가 해당 스터디에 속하는지 확인
        StudyNode studyNode = getStudyNode(studyId, nodeId);

        // 3. 해당 노드, 주차에서 ACCEPT 상태인 노드 후보 조회
        List<NodeCandidate> acceptedCandidates =
                nodeCandidateRepository
                        .findByStudyNode_IdAndState(
                                nodeId,
                                CandidateState.ACCEPTED
                        );

        // 승인된 노드 후보가 하나도 없다면 빈 리스트 반환
        if (acceptedCandidates.isEmpty()) {
            return NodeInfoRes.of(
                    studyNode.getId(),
                    List.of()
            );
        }

        // 4. 승인된 노드 후보 ID 목록 생성
        List<Long> candidateIds = acceptedCandidates.stream()
                .map(NodeCandidate::getId)
                .toList();

        // 5. 후보들과 연결된 스터디 자료 및 업로더 정보를 함께 조회
        List<NodeCandidate2StudyMaterial> relations =
                nodeCandidate2StudyMaterialRepository
                        .findAllWithMaterialAndMemberByCandidateIds(
                                candidateIds
                        );

        /*
         * 하나의 StudyMaterial이 여러 NodeCandidate와 연결되어 있을 경우
         * 같은 자료가 중복 반환될 수 있으므로 StudyMaterial ID 기준으로 제거
         */
        /*
        Map<Long, StudyMaterial> uniqueMaterials = new LinkedHashMap<>();

        for (NodeCandidate2StudyMaterial relation : relations) {
            StudyMaterial studyMaterial = relation.getStudyMaterial();

            uniqueMaterials.putIfAbsent(
                    studyMaterial.getId(),
                    studyMaterial
            );
        }

         */

        // 6. StudyMaterial을 응답 DTO로 변환
        List<NodeInfoRes.MaterialInfo> materials =
                uniqueMaterials.values().stream()
                        .map(NodeInfoRes.MaterialInfo::from)
                        .toList();

        // 7. 최종 응답 조립
        return NodeInfoRes.of(
                studyNode.getId(),
                materials
        );
    }


    /*

    내부 함수
     */


    private void validateStudyMember(
            Long studyId,
            Long memberId
    ) {
        memberStudyRepository
                .findByStudyIdAndMemberId(studyId, memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 스터디에 참여하고 있는 회원이 아닙니다."
                        )
                );
    }

    private StudyNode getStudyNode(
            Long studyId,
            Long nodeId
    ) {
        return studyNodeRepository
                .findByIdAndStudyId(nodeId, studyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 스터디에 존재하지 않는 노드입니다."
                        )
                );
    }

    private void validateWeek(Integer week) {
        if (week == null || week < 1) {
            throw new IllegalArgumentException(
                    "주차는 1 이상이어야 합니다."
            );
        }
    }
}



















//



