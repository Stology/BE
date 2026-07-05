package com.stology.be.domain.auth.service;

import com.stology.be.domain.auth.dto.AuthReqDTO;
import com.stology.be.domain.auth.dto.AuthResDTO;
import com.stology.be.domain.auth.exception.AuthException;
import com.stology.be.domain.auth.exception.code.AuthErrorCode;
import com.stology.be.global.security.entity.AuthMember;
import com.stology.be.global.security.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void reissue_withValidRefreshToken_returnsNewAccessAndRefreshTokens() {
        given(jwtUtil.isValid("valid-refresh-token")).willReturn(true);
        given(jwtUtil.getEmail("valid-refresh-token")).willReturn("test@test.com");
        given(jwtUtil.createAccessToken(any(AuthMember.class))).willReturn("new-access-token");
        given(jwtUtil.createRefreshToken(any(AuthMember.class))).willReturn("new-refresh-token");

        AuthResDTO.Login result = authService.reissue(new AuthReqDTO.Reissue("valid-refresh-token"));

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void reissue_withNullRefreshToken_throwsAuthExceptionWithoutCallingJwtUtil() {
        assertThatThrownBy(() -> authService.reissue(new AuthReqDTO.Reissue(null)))
                .isInstanceOf(AuthException.class)
                .extracting(ex -> ((AuthException) ex).getErrorCode())
                .isEqualTo(AuthErrorCode.AUTH_INVALID_REFRESH_TOKEN);

        verifyNoInteractions(jwtUtil);
    }

    @Test
    void reissue_withInvalidRefreshToken_throwsAuthException() {
        given(jwtUtil.isValid("bad-token")).willReturn(false);

        assertThatThrownBy(() -> authService.reissue(new AuthReqDTO.Reissue("bad-token")))
                .isInstanceOf(AuthException.class)
                .extracting(ex -> ((AuthException) ex).getErrorCode())
                .isEqualTo(AuthErrorCode.AUTH_INVALID_REFRESH_TOKEN);
    }

    @Test
    void reissue_withBlankRefreshToken_stillDelegatesValidationToJwtUtil() {
        given(jwtUtil.isValid("")).willReturn(false);

        assertThatThrownBy(() -> authService.reissue(new AuthReqDTO.Reissue("")))
                .isInstanceOf(AuthException.class);

        verify(jwtUtil).isValid("");
    }

    @Test
    void logout_doesNotThrowForAnyUsername() {
        assertThatCode(() -> authService.logout("test@test.com")).doesNotThrowAnyException();
    }

    @Test
    void delete_doesNotThrowForAnyUsername() {
        assertThatCode(() -> authService.delete("test@test.com")).doesNotThrowAnyException();
    }
}