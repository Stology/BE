package com.stology.be.domain.node.service;

import com.stology.be.domain.node.enums.CandidateState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CandidateReviewPolicyTest {

    private final CandidateReviewPolicy policy = new CandidateReviewPolicy();

    @Test
    void acceptsWhenApprovalCountMeetsReviewerCount() {
        CandidateState result = policy.decide(2, 0, 2);

        assertEquals(CandidateState.ACCEPT, result);
    }

    @Test
    void declinesWhenAReviewerDeclines() {
        CandidateState result = policy.decide(1, 1, 2);

        assertEquals(CandidateState.DECLINED, result);
    }

    @Test
    void remainsPendingWhileApprovalCountIsInsufficient() {
        CandidateState result = policy.decide(1, 0, 2);

        assertEquals(CandidateState.PENDING, result);
    }
}
