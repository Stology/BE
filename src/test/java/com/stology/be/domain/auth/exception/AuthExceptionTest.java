package com.stology.be.domain.auth.exception;

import com.stology.be.domain.auth.exception.code.AuthErrorCode;
import com.stology.be.global.apiPayload.exception.GeneralException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthExceptionTest {

    @Test
    void constructor_storesErrorCode() {
        AuthException exception = new AuthException(AuthErrorCode.AUTH_INVALID_REFRESH_TOKEN);

        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.AUTH_INVALID_REFRESH_TOKEN);
    }

    @Test
    void isInstanceOfGeneralExceptionAndRuntimeException() {
        AuthException exception = new AuthException(AuthErrorCode.AUTH_INVALID_REFRESH_TOKEN);

        assertThat(exception).isInstanceOf(GeneralException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}