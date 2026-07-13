package com.stology.be.domain.node.controller;


import com.stology.be.domain.node.dto.res.NodeExaminationInfoRes;
import com.stology.be.domain.node.exception.code.NodeSuccessCode;
import com.stology.be.domain.node.service.NodeVoteService;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.security.entity.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study/{studyId}")
public class StudyNodeFlowController {

    private final NodeVoteService nodeVoteService;

    /**
     * 노드 승인/반려
     */
    /*
    @PatchMapping("/acceptnode")
    public ApiResponse<Void> acceptNode(
            @PathVariable Long studyId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestBody AcceptNodeReq request
    ) {
        nodeService.acceptNode(
                studyId,
                authMember.memberId(),
                request
        );

        return ApiResponse.onSuccess(null);
    }

     */

    /**
     * 검토 정보 조회
     */
    @GetMapping("/node/get-examination-info")
    public ApiResponse<NodeExaminationInfoRes> getExaminationInfo(
            @PathVariable Long studyId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        NodeExaminationInfoRes response =
                nodeVoteService.getExaminationInfo(
                        studyId,
                        authMember.getMemberId()
                );

        return ApiResponse.onSuccess(NodeSuccessCode.GET_SUCCESS,response);
    }
}