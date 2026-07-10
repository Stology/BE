package com.stology.be.domain.auth.exception.code;

import com.stology.be.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthSuccessCode implements BaseSuccessCode {
    AUTH_LOGIN_SUCCESS(HttpStatus.UNAUTHORIZED,
            "AUTH200_1",
            "로그인에 성공하였습니다."),
    AUTH_REISSUE_SUCCESS(HttpStatus.OK,
            "AUTH200_2",
            "토큰이 재발급되었습니다."),
    AUTH_LOGOUT_SUCCESS(HttpStatus.NO_CONTENT,
            "AUTH204_1",
            "로그아웃에 성공하였습니다."),
    AUTH_DELETE_SUCCESS(HttpStatus.NO_CONTENT,
            "AUTH204_2",
            "성공적으로 계정을 삭제하였습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
