package com.stology.be.domain.auth.controller;

import com.stology.be.domain.auth.dto.AuthReqDTO;
import com.stology.be.domain.auth.dto.AuthResDTO;
import com.stology.be.domain.auth.service.AuthService;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.apiPayload.code.GeneralSuccessCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private Authentication authentication;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService);
    }

    @Test
    void reissue_delegatesToServiceAndWrapsResultInSuccessResponse() {
        AuthReqDTO.Reissue request = new AuthReqDTO.Reissue("refresh-token");
        AuthResDTO.Login serviceResult = new AuthResDTO.Login("access-token", "refresh-token");
        when(authService.reissue(request)).thenReturn(serviceResult);

        ApiResponse<AuthResDTO.Login> response = authController.reissue(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCode()).isEqualTo(GeneralSuccessCode.OK.getCode());
        assertThat(response.getResult()).isEqualTo(serviceResult);
    }

    @Test
    void logout_delegatesToServiceWithAuthenticatedUsername() {
        when(authentication.getName()).thenReturn("user@example.com");

        ApiResponse<Void> response = authController.logout(authentication);

        verify(authService).logout("user@example.com");
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCode()).isEqualTo(GeneralSuccessCode.OK.getCode());
        assertThat(response.getResult()).isNull();
    }

    @Test
    void deleteAccount_delegatesToServiceWithAuthenticatedUsername() {
        when(authentication.getName()).thenReturn("user@example.com");

        ApiResponse<Void> response = authController.deleteAccount(authentication);

        verify(authService).delete("user@example.com");
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCode()).isEqualTo(GeneralSuccessCode.OK.getCode());
        assertThat(response.getResult()).isNull();
    }
}