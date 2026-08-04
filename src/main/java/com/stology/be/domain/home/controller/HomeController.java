package com.stology.be.domain.home.controller;

import com.stology.be.domain.home.dto.res.*;
import com.stology.be.domain.home.service.HomeInfoService;
import com.stology.be.domain.home.service.HomeSpecificInfoService;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.apiPayload.code.GeneralSuccessCode;
import com.stology.be.global.security.entity.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class HomeController {

    private final HomeInfoService homeInfoService;
    private final HomeSpecificInfoService homeSpecificInfoService;

    /**
     * 내 할 일 조회
     */
    @GetMapping("/todo/me")
    public ApiResponse<MyTodoRes> getMyTodos(
            @AuthenticationPrincipal AuthMember authMember
    ) {
        MyTodoRes response =
                homeInfoService.getMyTodos(
                        authMember.getMemberId()
                );

        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                response
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
    @GetMapping("/materials")
    public ApiResponse<MaterialDetailRes> getMaterialDetail(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        MaterialDetailRes response =
                homeSpecificInfoService.getMaterialDetail(
                        authMember.getMemberId(),
                        cursor,
                        size
                );

        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                response
        );
    }

    /**
     * 질문 상세 조회
     */
    @GetMapping("/questions")
    public ApiResponse<QuestionDetailRes> getQuestionDetail(
            @RequestParam(required = false) Long cursor,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        QuestionDetailRes response =
                homeSpecificInfoService.getQuestionDetail(
                        authMember.getMemberId(),
                        cursor
                );

        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                response
        );
    }


    /**
     * 답변 상세 조회
     */
    @GetMapping("/answers")
    public ApiResponse<AnswerDetailRes> getAnswerDetail(
            @RequestParam(required = false) Long cursor,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        AnswerDetailRes response =
                homeSpecificInfoService.getAnswerDetail(
                        authMember.getMemberId(),
                        cursor
                );

        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                response
        );
    }

    /**
     * 리포트 상세 조회
     */
    @GetMapping("/reports")
    public ApiResponse<ReportDetailRes> getReportDetail(
            @RequestParam(required = false) Long cursor,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        ReportDetailRes response =
                homeSpecificInfoService.getReportDetail(
                        authMember.getMemberId(),
                        cursor
                );

        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                response
        );
    }
}