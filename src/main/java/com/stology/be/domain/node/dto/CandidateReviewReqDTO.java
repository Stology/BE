package com.stology.be.domain.node.dto;

import com.stology.be.domain.node.enums.ReviewDecision;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CandidateReviewReqDTO {

    public record Submit(
            @NotEmpty List<@Valid Decision> decisions
    ) {
    }

    public record Decision(
            @NotNull Long candidateId,
            @NotNull ReviewDecision decision
    ) {
    }
}
