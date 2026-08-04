package com.stology.be.domain.node.dto.req;

import com.stology.be.domain.node.enums.VoteType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AcceptNodeReq(

        @NotEmpty(message = "투표 항목은 한 개 이상이어야 합니다.")
        @Size(
                max = 20,
                message = "한 번에 최대 20개까지 투표할 수 있습니다."
        )
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