package com.stology.be.domain.node.service;

import com.stology.be.domain.node.enums.CandidateState;
import org.springframework.stereotype.Component;

@Component
public class CandidateReviewPolicy {

    public CandidateState decide(int acceptCount, int declineCount, int reviewerCount) {
        if (acceptCount >= reviewerCount) {
            return CandidateState.ACCEPT;
        }
        if (declineCount > 0) {
            return CandidateState.DECLINED;
        }
        return CandidateState.PENDING;
    }
}
