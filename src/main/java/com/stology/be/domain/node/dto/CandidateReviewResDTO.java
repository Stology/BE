package com.stology.be.domain.node.dto;

import com.stology.be.domain.node.enums.CandidateState;

import java.util.List;

public class CandidateReviewResDTO {

    public record Submit(
            Long materialId,
            List<Candidate> candidates
    ) {
    }

    public record Candidate(
            Long candidateId,
            CandidateState state
    ) {
    }
}
