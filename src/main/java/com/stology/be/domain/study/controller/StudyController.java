package com.stology.be.domain.study.controller;

import com.stology.be.domain.study.dto.StudyReqDTO;
import com.stology.be.domain.study.dto.StudyResDTO;
import com.stology.be.domain.study.exception.code.StudySuccessCode;
import com.stology.be.domain.study.service.StudyService;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.apiPayload.code.BaseSuccessCode;
import com.stology.be.global.security.entity.AuthMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class StudyController {

    private final StudyService studyService;

    // 스터디 방 생성
    @PostMapping("/study")
    public ApiResponse<Long> createStudy(
            @RequestBody @Valid StudyReqDTO.CreateStudy dto,
            @AuthenticationPrincipal AuthMember authMember
    ){
        BaseSuccessCode code = StudySuccessCode.STUDY_CREATE_SUCCESS;
        return ApiResponse.onSuccess(code, studyService.createStudy(dto, authMember.getMember()));
    }

    // 스터디 방 정보 수정
    @PatchMapping("/study/{studyId}")
    public ApiResponse<Void> updateStudy(
            @RequestBody @Valid StudyReqDTO.UpdateStudy dto,
            @PathVariable Long studyId,
            @AuthenticationPrincipal AuthMember authMember
    ){
        BaseSuccessCode code = StudySuccessCode.STUDY_UPDATE_SUCCESS;
        return ApiResponse.onSuccess(code, studyService.updateStudy(dto, studyId, authMember.getMember()));
    }

    // 스터디 방 삭제
    @DeleteMapping("/study/{studyId}")
    public ApiResponse<Void> deleteStudy(
            @PathVariable Long studyId,
            @AuthenticationPrincipal AuthMember authMember
    ){
        BaseSuccessCode code = StudySuccessCode.STUDY_DELETE_SUCCESS;
        return ApiResponse.onSuccess(code, studyService.deleteStudy(studyId, authMember.getMember()));
    }

    // 스터디 종료
    @PatchMapping("/study/{studyId}/close")
    public ApiResponse<Void> closeStudy(
            @PathVariable Long studyId,
            @AuthenticationPrincipal AuthMember authMember
    ){
        BaseSuccessCode code = StudySuccessCode.STUDY_CLOSED_SUCCESS;
        return ApiResponse.onSuccess(code, studyService.closeStudy(studyId, authMember.getMember()));
    }

    // 참여한 스터디 방 목록 조회
    @GetMapping("/user/me/study")
    public ApiResponse<StudyResDTO.GetStudy> getStudy(
            @RequestParam String status,
            @AuthenticationPrincipal AuthMember authMember
    ){
        BaseSuccessCode code = StudySuccessCode.STUDY_GET_SUCCESS;
        return ApiResponse.onSuccess(code, studyService.getStudy(status, authMember.getMember()));
    }


    // 온톨로지 템플릿 검색
    @GetMapping("/template")
    public ApiResponse<StudyResDTO.GetTemplate> getTemplate(
            @RequestParam(required = false) String search
    ){
        BaseSuccessCode code = StudySuccessCode.TEMPLATE_GET_SUCCESS;
        return ApiResponse.onSuccess(code, studyService.getTemplate(search));
    }

    // 검토 인원수 조회
    @GetMapping("/study/{studyId}/reviewer-count")
    public ApiResponse<StudyResDTO.GetReviewerCount> getReviewerCount(
            @PathVariable Long studyId
    ){
        BaseSuccessCode code = StudySuccessCode.REVIEWER_COUNT_GET_SUCCESS;
        return ApiResponse.onSuccess(code, studyService.getReviewerCount(studyId));
    }

    // 검토 인원수 조정
    @PatchMapping("/study/{studyId}/reviewer-count")
    public ApiResponse<Void> updateReviewerCount(
            @PathVariable Long studyId,
            @RequestBody @Valid StudyReqDTO.UpdateReviewerCount dto,
            @AuthenticationPrincipal AuthMember authMember
    ){
        BaseSuccessCode code = StudySuccessCode.REVIEWER_COUNT_UPDATE_SUCCESS;
        return ApiResponse.onSuccess(code, studyService.updateReviewerCount(studyId, dto, authMember.getMember()));
    }

    // 초대 토큰 생성
    @PostMapping("/study/{studyId}/invitation-token")
    public ApiResponse<String> createInvitationToken(
            @PathVariable Long studyId,
            @AuthenticationPrincipal AuthMember authMember
    ){
        BaseSuccessCode code = StudySuccessCode.INVITATION_TOKEN_CREATE_SUCCESS;
        return ApiResponse.onSuccess(code, studyService.createInvitationToken(studyId, authMember.getMember()));
    }

    // 초대 토큰 조회
    @GetMapping("/study/{studyId}/invitation-token")
    public ApiResponse<String> getInvitationToken(
            @PathVariable Long studyId,
            @AuthenticationPrincipal AuthMember authMember
    ){
        BaseSuccessCode code = StudySuccessCode.INVITATION_TOKEN_GET_SUCCESS;
        return ApiResponse.onSuccess(code, studyService.getInvitationToken(studyId, authMember.getMember()));
    }

    // 초대 토큰 수락
    @PostMapping("/study/invitation-token/accept")
    public ApiResponse<Void> acceptInvitationToken(
            @RequestParam String token,
            @AuthenticationPrincipal AuthMember authMember
    ){
        BaseSuccessCode code = StudySuccessCode.INVITATION_TOKEN_ACCEPT_SUCCESS;
        return ApiResponse.onSuccess(code, studyService.acceptInvitationToken(token, authMember.getMember()));
    }
}
