package com.stology.be.domain.node.controller;

import com.stology.be.domain.node.dto.CandidateReviewReqDTO;
import com.stology.be.domain.node.dto.CandidateReviewResDTO;
import com.stology.be.domain.node.service.CandidateReviewService;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.apiPayload.code.GeneralSuccessCode;
import com.stology.be.global.security.entity.AuthMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/studies/{studyId}/materials/{materialId}/reviews")
@RequiredArgsConstructor
public class CandidateReviewController {

    private final CandidateReviewService candidateReviewService;

    @PostMapping
    public ApiResponse<CandidateReviewResDTO.Submit> submit(
            @PathVariable Long studyId,
            @PathVariable Long materialId,
            @AuthenticationPrincipal AuthMember authMember,
            @Valid @RequestBody CandidateReviewReqDTO.Submit request
    ) {
        CandidateReviewResDTO.Submit result = candidateReviewService.submit(
                studyId,
                materialId,
                authMember.getMember(),
                request);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }
}
