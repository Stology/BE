package com.stology.be.domain.node.service;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.node.dto.NodeVoteInfoDto;
import com.stology.be.domain.node.dto.req.AcceptNodeReq;
import com.stology.be.domain.node.dto.res.AcceptNodeRes;
import com.stology.be.domain.node.dto.res.NodeExaminationInfoRes;
import com.stology.be.domain.node.entity.NodeCandidate;
import com.stology.be.domain.node.entity.NodeCandidateVoteInfo;
import com.stology.be.domain.node.enums.CandidateState;
import com.stology.be.domain.node.repository.NodeCandidateRepository;
import com.stology.be.domain.node.repository.NodeCandidateVoteInfoRepository;
import com.stology.be.domain.study.repository.MemberStudyRepository;
import com.stology.be.global.security.entity.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class NodeVoteService {

    private final MemberStudyRepository memberStudyRepository;
    private final NodeCandidateRepository nodeCandidateRepository;
    private final NodeCandidateVoteInfoRepository nodeCandidateVoteInfoRepository;


    @Transactional(readOnly = true)
    public NodeExaminationInfoRes getExaminationInfo(Long studyId,Long memberId) {


        int numberOfStudyMembers =
                Math.toIntExact(memberStudyRepository.countByStudyId(studyId));

        List<NodeVoteInfoDto> rows =
                nodeCandidateRepository.findPendingVoteInfos(studyId, CandidateState.PENDING);

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

    @Transactional
    public AcceptNodeRes vote(
            Long studyId,
            AuthMember member,
            AcceptNodeReq request
    ) {
        //
        validateStudyMember(studyId, member.getMemberId());


        List<AcceptNodeRes.AcceptInfo> acceptInfos = new ArrayList<>();

        for (AcceptNodeReq.NodeVoteReq voteRequest : request.votes()) {
            acceptInfos.add(
                    processVote(
                            studyId,
                            member.getMember(),
                            voteRequest
                    )
            );
        }
        // 스터디 노드 활성화 정도 조절 로직 추가 예정. activation_week, activation_level을 추가해야함.


        return AcceptNodeRes.from(acceptInfos);

    }






    private AcceptNodeRes.AcceptInfo processVote(
            Long studyId,
            Member member,
            AcceptNodeReq.NodeVoteReq request
    ) {
        //검증
        NodeCandidate nodeCandidate = validateNodeCandidate(request,studyId);


        NodeCandidateVoteInfo voteInfo =
                nodeCandidateVoteInfoRepository
                        .findByNodeCandidate_IdAndMember_Id(
                                nodeCandidate.getId(),
                                member.getId()
                        )
                        .orElse(null);

        if (voteInfo == null) {
            voteInfo = NodeCandidateVoteInfo.builder()
                    .nodeCandidate(nodeCandidate)
                    .member(member)
                    .voteType(request.voteType())
                    .build();
        } else {
            voteInfo.updateVote(request.voteType());
        }

        nodeCandidateVoteInfoRepository.save(voteInfo);

        return AcceptNodeRes.AcceptInfo.of(
                request.studyNodeId(),
                request.nodeCandidateId()
        );
    }

    private void validateStudyMember(
            Long studyId,
            Long memberId
    ) {
        boolean isStudyMember =
                memberStudyRepository
                        .existsByStudyIdAndMemberId(
                                studyId,
                                memberId
                        );

        if (!isStudyMember) {
            throw new IllegalArgumentException(
                    "해당 스터디에 참여 중인 회원이 아닙니다."
            );
        }
    }
    private NodeCandidate validateNodeCandidate(AcceptNodeReq.NodeVoteReq request, Long studyId)
    {
        NodeCandidate nodeCandidate =
                nodeCandidateRepository
                        .findByIdAndStudyNode_IdAndStudyNode_Study_IdAndState(
                                request.nodeCandidateId(),
                                request.studyNodeId(),
                                studyId,
                                CandidateState.PENDING
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "해당 스터디 노드에 속한 " +
                                                "검토 중인 노드 후보가 아닙니다."
                                )
                        );
        return nodeCandidate;
    }

}
