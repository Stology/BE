package com.stology.be.domain.node.controller;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.node.dto.CandidateReviewReqDTO;
import com.stology.be.domain.node.dto.CandidateReviewResDTO;
import com.stology.be.domain.node.enums.CandidateState;
import com.stology.be.domain.node.enums.ReviewDecision;
import com.stology.be.domain.node.service.CandidateReviewService;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.security.entity.AuthMember;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CandidateReviewControllerTest {

    @Test
    void delegatesAuthenticatedMemberAndWrapsSubmissionResult() {
        CandidateReviewService service = mock(CandidateReviewService.class);
        CandidateReviewController controller = new CandidateReviewController(service);
        Member member = Member.builder().id(1L).name("reviewer").build();
        AuthMember authMember = new AuthMember(member);
        CandidateReviewReqDTO.Submit request = new CandidateReviewReqDTO.Submit(List.of(
                new CandidateReviewReqDTO.Decision(100L, ReviewDecision.ACCEPT)
        ));
        CandidateReviewResDTO.Submit serviceResult = new CandidateReviewResDTO.Submit(
                20L,
                List.of(new CandidateReviewResDTO.Candidate(100L, CandidateState.ACCEPT))
        );
        when(service.submit(10L, 20L, member, request)).thenReturn(serviceResult);

        ApiResponse<CandidateReviewResDTO.Submit> response =
                controller.submit(10L, 20L, authMember, request);

        assertTrue(response.isSuccess());
        assertEquals(serviceResult, response.getResult());
        verify(service).submit(10L, 20L, member, request);
    }
}
