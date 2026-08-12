package com.stology.be.domain.home.exception.code;

import com.stology.be.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HomeSuccessCode implements BaseSuccessCode {

    MY_TODO_GET_SUCCESS(
            HttpStatus.OK,
            "HOME200_1",
            "내 할 일 조회에 성공했습니다."
    ),

    TEAM_ACTIVITY_GET_SUCCESS(
            HttpStatus.OK,
            "HOME200_2",
            "팀 활동 조회에 성공했습니다."
    ),

    MATERIAL_DETAIL_GET_SUCCESS(
            HttpStatus.OK,
            "HOME200_3",
            "자료 상세 목록 조회에 성공했습니다."
    ),

    QUESTION_DETAIL_GET_SUCCESS(
            HttpStatus.OK,
            "HOME200_4",
            "질문 상세 목록 조회에 성공했습니다."
    ),

    ANSWER_DETAIL_GET_SUCCESS(
            HttpStatus.OK,
            "HOME200_5",
            "답글 상세 목록 조회에 성공했습니다."
    ),

    REPORT_DETAIL_GET_SUCCESS(
            HttpStatus.OK,
            "HOME200_6",
            "리포트 상세 목록 조회에 성공했습니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}