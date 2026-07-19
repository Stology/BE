package com.stology.be.domain.node.entity;

import com.stology.be.domain.node.enums.CandidateState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NodeCandidateTest {

    @Test
    void changesCandidateState() {
        NodeCandidate candidate = NodeCandidate.builder().build();

        candidate.updateReviewResult(CandidateState.ACCEPT, 2);

        assertEquals(CandidateState.ACCEPT, candidate.getState());
        assertEquals(2, candidate.getAcceptCount());
    }
}
