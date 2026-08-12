package com.stology.be.domain.node.controller;

import com.stology.be.domain.node.dto.res.KnowledgeGraphResponseDto.GraphRes;
import com.stology.be.domain.node.dto.res.KnowledgeGraphResponseDto.NodeDetailRes;
import com.stology.be.domain.node.service.KnowledgeGraphService;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.apiPayload.code.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study")
public class KnowledgeGraphController {

    private final KnowledgeGraphService knowledgeGraphService;

    @GetMapping("/{studyId}/knowledge-graph")
    public ApiResponse<GraphRes> getKnowledgeGraph(
            @PathVariable("studyId") Long studyId
    ) {
        GraphRes graph = knowledgeGraphService.getKnowledgeGraph(studyId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, graph);
    }

    @GetMapping("/{studyId}/knowledge-graph/nodes/{nodeId}")
    public ApiResponse<NodeDetailRes> getNodeDetail(
            @PathVariable("studyId") Long studyId,
            @PathVariable("nodeId") Long nodeId
    ) {
        NodeDetailRes nodeDetail = knowledgeGraphService.getNodeDetail(studyId, nodeId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, nodeDetail);
    }
}
