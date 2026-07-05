package com.stology.be.domain.auth.service;

import com.stology.be.domain.auth.dto.AuthReqDTO;
import com.stology.be.domain.auth.dto.AuthResDTO;
import com.stology.be.domain.auth.exception.AuthException;
import com.stology.be.domain.auth.exception.code.AuthErrorCode;
import com.stology.be.global.security.entity.AuthMember;
import com.stology.be.global.security.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(jwtUtil);
    }

    @Test
    void reissue_withValidRefreshToken_returnsNewTokenPair() {
        when(jwtUtil.isValid("valid-refresh-token")).thenReturn(true);
        when(jwtUtil.getEmail("valid-refresh-token")).thenReturn("user@example.com");
        when(jwtUtil.createAccessToken(any(AuthMember.class))).thenReturn("new-access-token");
        when(jwtUtil.createRefreshToken(any(AuthMember.class))).thenReturn("new-refresh-token");

        AuthResDTO.Login result = authService.reissue(new AuthReqDTO.Reissue("valid-refresh-token"));

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void reissue_withNullRefreshToken_throwsAuthException() {
        assertThatThrownBy(() -> authService.reissue(new AuthReqDTO.Reissue(null)))
                .isInstanceOf(AuthException.class)
                .satisfies(ex -> assertThat(((AuthException) ex).getErrorCode())
                        .isEqualTo(AuthErrorCode.AUTH_INVALID_REFRESH_TOKEN));

        verify(jwtUtil, never()).isValid(anyString());
    }

    @Test
    void reissue_withInvalidRefreshToken_throwsAuthException() {
        when(jwtUtil.isValid("invalid-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.reissue(new AuthReqDTO.Reissue("invalid-token")))
                .isInstanceOf(AuthException.class)
                .satisfies(ex -> assertThat(((AuthException) ex).getErrorCode())
                        .isEqualTo(AuthErrorCode.AUTH_INVALID_REFRESH_TOKEN));

        verify(jwtUtil, never()).createAccessToken(any());
        verify(jwtUtil, never()).createRefreshToken(any());
    }

    @Test
    void logout_doesNotThrow_forAnyUsername() {
        assertThatCodeDoesNotThrow(() -> authService.logout("user@example.com"));
        assertThatCodeDoesNotThrow(() -> authService.logout(null));
    }

    @Test
    void delete_doesNotThrow_forAnyUsername() {
        assertThatCodeDoesNotThrow(() -> authService.delete("user@example.com"));
        assertThatCodeDoesNotThrow(() -> authService.delete(null));
    }

    private void assertThatCodeDoesNotThrow(Runnable runnable) {
        org.assertj.core.api.Assertions.assertThatCode(runnable::run).doesNotThrowAnyException();
    }
}