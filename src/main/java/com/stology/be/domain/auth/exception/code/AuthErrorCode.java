package com.stology.be.domain.auth.exception.code;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
    AUTH_INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED,
            "AUTH401_1",
            "유효하지 않은 Access Token입니다."),
    AUTH_INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,
            "AUTH401_2",
            "유효하지 않은 Refresh Token입니다."),
    AUTH_REFRESH_TOKEN_MISSING(HttpStatus.BAD_REQUEST,
            "AUTH_400_1",
            "Refresh Token은 필수입니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
