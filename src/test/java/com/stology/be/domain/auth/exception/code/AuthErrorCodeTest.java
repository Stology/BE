package com.stology.be.domain.auth.exception.code;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class AuthErrorCodeTest {

    @Test
    void authInvalidRefreshToken_hasExpectedAttributes() {
        AuthErrorCode code = AuthErrorCode.AUTH_INVALID_REFRESH_TOKEN;

        assertThat(code.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(code.getCode()).isEqualTo("AUTH400_1");
        assertThat(code.getMessage()).isEqualTo("유효하지 않은 Refresh Token입니다.");
    }

    @Test
    void implementsBaseErrorCode() {
        assertThat(AuthErrorCode.AUTH_INVALID_REFRESH_TOKEN)
                .isInstanceOf(com.stology.be.global.apiPayload.code.BaseErrorCode.class);
    }

    @Test
    void values_containsExactlyOneEntry() {
        assertThat(AuthErrorCode.values()).containsExactly(AuthErrorCode.AUTH_INVALID_REFRESH_TOKEN);
    }
}