package com.stology.be.domain.node.service;

import com.stology.be.domain.node.dto.NodeVoteInfoDto;
import com.stology.be.domain.node.dto.res.NodeExaminationInfoRes;
import com.stology.be.domain.node.enums.CandidateState;
import com.stology.be.domain.node.repository.NodeCandidateRepository;
import com.stology.be.domain.node.repository.NodeCandidateVoteInfoRepository;
import com.stology.be.domain.study.repository.MemberStudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class NodeVoteService {

    MemberStudyRepository memberStudyRepository;
    NodeCandidateRepository nodeCandidateRepository;
    NodeCandidateVoteInfoRepository nodeCandidateVoteInfoRepository;


    @Transactional(readOnly = true)
    public NodeExaminationInfoRes getExaminationInfo(Long studyId,Long memberId) {


        int numberOfStudyMembers =
                Math.toIntExact(memberStudyRepository.countByStudyId(studyId));

        List<NodeVoteInfoDto> rows =
                nodeCandidateVoteInfoRepository.findPendingVoteInfos(studyId, CandidateState.PENDING);

        Map<Long, List<NodeVoteInfoDto>> grouped =
                rows.stream()
                        .collect(Collectors.groupingBy(
                                NodeVoteInfoDto::nodeCandidateId,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        List<NodeExaminationInfoRes.NodeCandidateVoteInfo> nodeCandidates =
                grouped.values()
                        .stream()
                        .map(candidateRows -> {

                            NodeVoteInfoDto first = candidateRows.get(0);

                            List<NodeExaminationInfoRes.MemberVoteInfo> memberVoteInfos =
                                    candidateRows.stream()
                                            .map(row ->
                                                    NodeExaminationInfoRes.MemberVoteInfo.of(
                                                            row.memberId(),
                                                            row.memberName(),
                                                            row.voteType()
                                                    )
                                            )
                                            .toList();

                            return NodeExaminationInfoRes.NodeCandidateVoteInfo.of(
                                    first.nodeCandidateId(),
                                    first.studyNodeId(),
                                    numberOfStudyMembers,
                                    first.acceptCount(),
                                    memberVoteInfos
                            );
                        })
                        .toList();

        return NodeExaminationInfoRes.from(nodeCandidates);
    }
}
