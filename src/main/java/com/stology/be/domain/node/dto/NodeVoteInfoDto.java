package com.stology.be.domain.node.dto;

import com.stology.be.domain.node.enums.VoteType;

public record NodeVoteInfoDto(

        Long nodeCandidateId,
        Long studyNodeId,
        Integer acceptCount,

        Long memberId,
        String memberName,
        VoteType voteType

) {
}