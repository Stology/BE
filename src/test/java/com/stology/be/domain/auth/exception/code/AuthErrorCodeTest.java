package com.stology.be.domain.auth.exception.code;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class AuthErrorCodeTest {

    @Test
    void authInvalidRefreshToken_hasExpectedHttpStatusCodeAndMessage() {
        AuthErrorCode errorCode = AuthErrorCode.AUTH_INVALID_REFRESH_TOKEN;

        assertThat(errorCode.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorCode.getCode()).isEqualTo("AUTH400_1");
        assertThat(errorCode.getMessage()).isEqualTo("유효하지 않은 Refresh Token입니다.");
    }

    @Test
    void implementsBaseErrorCode() {
        assertThat(AuthErrorCode.AUTH_INVALID_REFRESH_TOKEN)
                .isInstanceOf(com.stology.be.global.apiPayload.code.BaseErrorCode.class);
    }
}