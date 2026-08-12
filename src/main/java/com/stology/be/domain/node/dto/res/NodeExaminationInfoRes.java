package com.stology.be.domain.node.dto.res;

import com.stology.be.domain.node.enums.VoteType;

import java.util.List;


public record NodeExaminationInfoRes(
        List<NodeCandidateVoteInfo> nodeCandidates
) {

    public static NodeExaminationInfoRes from(
            List<NodeCandidateVoteInfo> nodeCandidates
    ) {
        return new NodeExaminationInfoRes(nodeCandidates);
    }

    public record NodeCandidateVoteInfo(
            Long nodeCandidateId,
            Long studyNodeId,
            int numberOfStudyMembers,
            int numberOfAcceptedStudyMembers,
            List<MemberVoteInfo> memberVoteInfos
    ) {

        public static NodeCandidateVoteInfo of(
                Long nodeCandidateId,
                Long studyNodeId,
                int numberOfStudyMembers,
                int numberOfAcceptedStudyMembers,
                List<MemberVoteInfo> memberVoteInfos
        ) {
            return new NodeCandidateVoteInfo(
                    nodeCandidateId,
                    studyNodeId,
                    numberOfStudyMembers,
                    numberOfAcceptedStudyMembers,
                    memberVoteInfos
            );
        }
    }

    public record MemberVoteInfo(
            Long memberId,
            String name,
            VoteType voteType
    ) {

        public static MemberVoteInfo of(
                Long memberId,
                String name,
                VoteType voteType
        ) {
            return new MemberVoteInfo(
                    memberId,
                    name,
                    voteType
            );
        }
    }
}