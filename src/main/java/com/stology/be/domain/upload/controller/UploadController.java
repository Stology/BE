package com.stology.be.domain.upload.controller;

import com.stology.be.domain.upload.Component.SseEmitterRepository;
import com.stology.be.domain.upload.dto.res.SseConnectRes;
import com.stology.be.domain.upload.exception.code.UploadSuccessCode;
import com.stology.be.domain.upload.service.SseService;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.security.entity.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/study/{studyId}")
@RequiredArgsConstructor
public class UploadController {

    private final SseEmitterRepository repository;
    private final SseService sseService;

    /**
     * 실시간 자료 업로드
     * POST /api/study/{studyId}/uploadSSE
     */
    @GetMapping("/uploadSSE")
    public ApiResponse<SseConnectRes> uploadSSE(
            @PathVariable Long studyId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        // TODO: SSE 연결 및 실시간 업로드 상태 전송
        return ApiResponse.onSuccess(UploadSuccessCode.UPLOAD_SUCCESS,
                SseConnectRes.builder()
                        .emitter(sseService.subscribe(studyId, authMember.getMemberId()))
                        .build()
        );
    }

    /**
     * 자료 업로드
     * POST /api/study/{studyId}/upload
     */
    @PostMapping("/upload")
    public ApiResponse<Void> upload(
            @PathVariable Long studyId,
            @RequestPart("file") MultipartFile file
    ) {
        // TODO: 파일 업로드 처리

        return ApiResponse.onSuccess(UploadSuccessCode.UPLOAD_SUCCESS,null);
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