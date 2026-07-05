package com.stology.be.domain.auth.exception.code;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
    AUTH_INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST,
            "AUTH400_1",
            "유효하지 않은 Refresh Token입니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
