package com.stology.be.domain.node.controller;

import com.stology.be.domain.node.dto.res.NodeInfoRes;
import com.stology.be.domain.node.dto.res.WeekNodeRes;
import com.stology.be.domain.node.exception.code.NodeSuccessCode;
import com.stology.be.domain.node.service.WeekNodeService;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.security.entity.AuthMember;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study/{studyId}")
public class ActiveNodeController {

    private final WeekNodeService weekNodeService;

    @GetMapping("/active-nodes")
    public ApiResponse<WeekNodeRes> getActiveNode(
            @PathVariable Long studyId,
            @RequestParam int week,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        return ApiResponse.onSuccess(NodeSuccessCode.ACTIVE_NODES_GET_SUCCESS,
                weekNodeService.getWeekNodes(studyId, week, authMember.getMemberId())
        );
    }

    @GetMapping("/node/{nodeId}/info")
    public ApiResponse<NodeInfoRes> getNodeInfo(
            @PathVariable Long studyId,
            @PathVariable Long nodeId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        return ApiResponse.onSuccess(
                NodeSuccessCode.NODE_INFO_GET_SUCCESS,
                weekNodeService.getNodeInfo(
                        studyId,
                        nodeId,
                        authMember.getMemberId()
                )
        );
    }
}