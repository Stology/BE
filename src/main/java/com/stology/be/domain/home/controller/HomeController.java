package com.stology.be.domain.home.controller;

import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.apiPayload.code.GeneralSuccessCode;
import com.stology.be.global.security.entity.AuthMember;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    /**
     * 내 할 일 조회
     */
    @GetMapping("/todo/me")
    public ApiResponse<Void> getMyTodos(
            @AuthenticationPrincipal AuthMember authMember
    ) {
        // TODO:
        // homeService.getMyTodos(
        //         authMember.getMemberId()
        // );

        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                null
        );
    }

    /**
     * 특정 스터디의 팀 활동 조회
     * studyId가 -1이라면 전체스터디 조회.
     * 나머진 특정 스터디 에서의 조회.
     */
    @GetMapping("/studies/{studyId}/actives")
    public ApiResponse<Void> getTeamTodos(
            @PathVariable Long studyId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        // TODO:
        // homeService.getTeamTodos(
        //         studyId,
        //         authMember.getMemberId()
        // );

        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                null
        );
    }

    /**
     * 자료 관련 상세 조회
     */
    @GetMapping(
            "/materials"
    )
    public ApiResponse<Void> getMaterialDetail(
            @AuthenticationPrincipal AuthMember authMember
    ) {
        // TODO:
        // homeService.getMaterialDetail(
        //         studyId,
        //         studyMaterialId,
        //         authMember.getMemberId()
        // );

        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                null
        );
    }

    /**
     * 질문함 상세 조회
     */
    @GetMapping(
            "/questions"
    )
    public ApiResponse<Void> getQuestionDetail(
            @PathVariable Long studyId,
            @PathVariable Long questionId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        // TODO:
        // homeService.getQuestionDetail(
        //         studyId,
        //         questionId,
        //         authMember.getMemberId()
        // );

        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                null
        );
    }

    /**
     * 리포트 상세 조회
     */
    @GetMapping(
            "/reports"
    )
    public ApiResponse<Void> getReportDetail(
            @AuthenticationPrincipal AuthMember authMember
    ) {
        // TODO:
        // homeService.getReportDetail(
        //         studyId,
        //         reportId,
        //         authMember.getMemberId()
        // );

        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                null
        );
    }
}