package com.stology.be.domain.node.service;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.node.dto.NodeVoteInfoDto;
import com.stology.be.domain.node.dto.req.AcceptNodeReq;
import com.stology.be.domain.node.dto.res.AcceptNodeRes;
import com.stology.be.domain.node.dto.res.NodeExaminationInfoRes;
import com.stology.be.domain.node.entity.NodeCandidate;
import com.stology.be.domain.node.entity.NodeCandidateVoteInfo;
import com.stology.be.domain.node.entity.StudyNode;
import com.stology.be.domain.node.enums.CandidateState;
import com.stology.be.domain.node.enums.VoteType;
import com.stology.be.domain.node.repository.NodeCandidateRepository;
import com.stology.be.domain.node.repository.NodeCandidateVoteInfoRepository;
import com.stology.be.domain.node.repository.StudyNodeRepository;
import com.stology.be.domain.study.entity.Study;
import com.stology.be.domain.study.exception.StudyException;
import com.stology.be.domain.study.exception.code.StudyErrorCode;
import com.stology.be.domain.study.repository.MemberStudyRepository;
import com.stology.be.domain.study.repository.StudyRepository;
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
    private final StudyRepository studyRepository;
    private final StudyNodeRepository studyNodeRepository;


    @Transactional(readOnly = true)
    public NodeExaminationInfoRes getExaminationInfo(
            Long studyId,
            Long memberId
    ) {
        //스터디에 속하는지 검증
        validateStudyMember(studyId, memberId);
        // 검토 인원수 찾기
        int numberOfStudyMembers =
                getNumberOfStudyMembers(studyId);

        //다음으로 검토 중인 후보와 투표 내역을 조회합니다.
        List<NodeVoteInfoDto> rows =
                getPendingVoteInfos(studyId);

        //조회 결과를 노드 후보 ID 기준으로 묶습니다.
        Map<Long, List<NodeVoteInfoDto>> groupedRows =
                groupByNodeCandidate(rows);

        //후보별 응답 DTO로 변환합니다.
        List<NodeExaminationInfoRes.NodeCandidateVoteInfo>
                nodeCandidates =
                toNodeCandidateVoteInfos(
                        groupedRows,
                        numberOfStudyMembers
                );

        return NodeExaminationInfoRes.from(nodeCandidates);
    }

    //동일 스터디 안에 있는 노드 후보들
    @Transactional
    public AcceptNodeRes vote(
            Long studyId,
            AuthMember member,
            AcceptNodeReq request
    ) {

        //노드 활성화 하기 위해 몇명이상 필요한지.
        int numberOfNeedAcceptMember =
                getNumberOfStudyMembers(studyId);
        //검증
        validateStudyMember(studyId, member.getMemberId());


        List<AcceptNodeRes.AcceptInfo> acceptInfos = new ArrayList<>();

        for (AcceptNodeReq.NodeVoteReq voteRequest : request.votes()) {
            acceptInfos.add(
                    processVote(
                            studyId,
                            member.getMember(),
                            voteRequest,
                            numberOfNeedAcceptMember
                    )
            );
        }
        // 스터디 노드 활성화 정도 조절 로직 추가 예정. activation_week, activation_level을 추가해야함.


        return AcceptNodeRes.from(acceptInfos);

    }


    /**
     *
     *
     * getExamination의 내부 함수
     *
     *
     *
     */

    private int getNumberOfStudyMembers(Long studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() ->
                        new StudyException(
                                StudyErrorCode.STUDY_NOT_FOUND
                        )
                );

        return study.getReviewerCount();
    }

    private List<NodeVoteInfoDto> getPendingVoteInfos(
            Long studyId
    ) {
        return nodeCandidateRepository.findPendingVoteInfos(
                studyId,
                CandidateState.PENDING
        );
    }

    private Map<Long, List<NodeVoteInfoDto>> groupByNodeCandidate(
            List<NodeVoteInfoDto> rows
    ) {
        return rows.stream()
                .collect(Collectors.groupingBy(
                        NodeVoteInfoDto::nodeCandidateId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private List<NodeExaminationInfoRes.NodeCandidateVoteInfo>
    toNodeCandidateVoteInfos(
            Map<Long, List<NodeVoteInfoDto>> groupedRows,
            int numberOfStudyMembers
    ) {
        return groupedRows.values()
                .stream()
                .map(candidateRows ->
                        toNodeCandidateVoteInfo(
                                candidateRows,
                                numberOfStudyMembers
                        )
                )
                .toList();
    }

    private NodeExaminationInfoRes.NodeCandidateVoteInfo
    toNodeCandidateVoteInfo(
            List<NodeVoteInfoDto> candidateRows,
            int numberOfStudyMembers
    ) {
        NodeVoteInfoDto first = candidateRows.get(0);

        List<NodeExaminationInfoRes.MemberVoteInfo>
                memberVoteInfos =
                toMemberVoteInfos(candidateRows);

        return NodeExaminationInfoRes.NodeCandidateVoteInfo.of(
                first.nodeCandidateId(),
                first.studyNodeId(),
                numberOfStudyMembers,
                first.acceptCount(),
                memberVoteInfos
        );
    }

    private List<NodeExaminationInfoRes.MemberVoteInfo>
    toMemberVoteInfos(
            List<NodeVoteInfoDto> candidateRows
    ) {
        return candidateRows.stream()
                .filter(row -> row.memberId() != null)
                .map(row ->
                        NodeExaminationInfoRes.MemberVoteInfo.of(
                                row.memberId(),
                                row.memberName(),
                                row.voteType()
                        )
                )
                .toList();
    }

    /**
     *
     *
     * Vote의 내부 함수
     *
     *
     *
     */

    private AcceptNodeRes.AcceptInfo processVote(
            Long studyId,
            Member member,
            AcceptNodeReq.NodeVoteReq request,
            int numberOfNeedAcceptMember
    ) {
        NodeCandidate nodeCandidate =
                validateNodeCandidate(request, studyId);

        //투표 갱신
        saveOrUpdateVote(
                nodeCandidate,
                member,
                request.voteType()
        );

        // 투표 수 저장
        updateCandidateApproval(
                nodeCandidate,
                request.studyNodeId(),
                numberOfNeedAcceptMember
        );

        return AcceptNodeRes.AcceptInfo.of(
                request.studyNodeId(),
                request.nodeCandidateId()
        );
    }


    private void saveOrUpdateVote(
            NodeCandidate nodeCandidate,
            Member member,
            VoteType voteType
    ) {
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
                    .voteType(voteType)
                    .build();
        } else {
            voteInfo.updateVote(voteType);
        }

        nodeCandidateVoteInfoRepository.save(voteInfo);
    }

    private void updateCandidateApproval(
            NodeCandidate nodeCandidate,
            Long studyNodeId,
            int requiredAcceptCount
    ) {
        int acceptCount = Math.toIntExact(
                nodeCandidateVoteInfoRepository
                        .countByNodeCandidate_IdAndVoteType(
                                nodeCandidate.getId(),
                                VoteType.ACCEPT
                        )
        );

        nodeCandidate.updateAcceptCount(acceptCount);

        if (acceptCount < requiredAcceptCount) {
            return;
        }

        nodeCandidate.changeState(
                CandidateState.ACCEPTED
        );

        increaseStudyNodeActiveLevel(studyNodeId);
    }


    private void increaseStudyNodeActiveLevel(
            Long studyNodeId
    ) {
        StudyNode studyNode =
                studyNodeRepository
                        .findByIdForUpdate(studyNodeId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "스터디 노드를 찾을 수 없습니다."
                                )
                        );

        studyNode.increaseActiveLevel();
    }

    /*
    내부 공용 함수.

     */
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
        return
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
    }

}
