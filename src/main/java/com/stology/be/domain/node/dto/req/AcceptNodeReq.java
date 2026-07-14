package com.stology.be.domain.node.dto.req;

import com.stology.be.domain.node.enums.VoteType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AcceptNodeReq(

        @NotEmpty
        List<@Valid NodeVoteReq> votes

) {

    public record NodeVoteReq(

            @NotNull
            Long studyNodeId,

            @NotNull
            Long nodeCandidateId,

            @NotNull
            VoteType voteType
    ) {
    }
}