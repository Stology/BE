package com.stology.be.domain.upload.controller;

import com.stology.be.domain.upload.component.SseEmitterRepository;
import com.stology.be.domain.upload.dto.req.UploadReq;
import com.stology.be.domain.upload.dto.res.RecentFilesRes;
import com.stology.be.domain.upload.dto.res.SseConnectRes;
import com.stology.be.domain.upload.exception.code.UploadSuccessCode;
import com.stology.be.domain.upload.service.SseService;
import com.stology.be.domain.upload.service.UploadService;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.security.entity.AuthMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/study/{studyId}")
@RequiredArgsConstructor
public class UploadController {

    private final SseEmitterRepository repository;
    private final SseService sseService;
    private final UploadService uploadService;

    /**
     * 실시간 자료 업로드
     * POST /api/study/{studyId}/uploadSSE
     */
    @GetMapping(
            value = "/uploadSSE",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter uploadSSE(
            @PathVariable Long studyId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        return sseService.subscribe(
                studyId,
                authMember.getMemberId()
        );
    }

    /**
     * 자료 업로드
     * POST /api/study/{studyId}/upload
     */
    @PostMapping(value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<Void> upload(
            @PathVariable Long studyId,
            @AuthenticationPrincipal AuthMember authMember,
            @Valid @ModelAttribute UploadReq request
    ) {
        // TODO: 파일 업로드 처리
        uploadService.upload(studyId,authMember.getMemberId(),request);


        return ApiResponse.onSuccess(UploadSuccessCode.UPLOAD_SUCCESS,null);
    }
    /**
     * 스터디 자료 조회
     * POST /api/study/{studyId}/upload
     */

    @GetMapping("/upload")
    public ApiResponse<RecentFilesRes> getStudyUploadFiles (
            @PathVariable Long studyId,
            @AuthenticationPrincipal AuthMember authMember
    ) {


        RecentFilesRes result = uploadService.getStudyUploadFiles(studyId,authMember.getMemberId());

        return ApiResponse.onSuccess(UploadSuccessCode.UPLOAD_SUCCESS,result);
    }

    /**
     * 자료 AI 분석
     * GET /api/study/{studyId}/analyze
     */
    @GetMapping("/analyze")
    public ResponseEntity<Void> analyze(
            @PathVariable Long studyId
    ) {
        // TODO: 업로드된 자료 AI 분석

        return ResponseEntity.ok().build();
    }
}