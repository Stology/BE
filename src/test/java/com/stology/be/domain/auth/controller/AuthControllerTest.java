package com.stology.be.domain.auth.controller;

import com.stology.be.domain.auth.dto.AuthReqDTO;
import com.stology.be.domain.auth.dto.AuthResDTO;
import com.stology.be.domain.auth.service.AuthService;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.apiPayload.code.GeneralSuccessCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    @Test
    void reissue_delegatesToServiceAndWrapsResultInSuccessResponse() {
        AuthReqDTO.Reissue request = new AuthReqDTO.Reissue("refresh-token");
        AuthResDTO.Login expected = new AuthResDTO.Login("access-token", "refresh-token");
        given(authService.reissue(request)).willReturn(expected);

        ApiResponse<AuthResDTO.Login> response = authController.reissue(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCode()).isEqualTo(GeneralSuccessCode.OK.getCode());
        assertThat(response.getResult()).isEqualTo(expected);
    }

    @Test
    void logout_usesAuthenticationNameToInvokeService() {
        given(authentication.getName()).willReturn("test@test.com");

        ApiResponse<Void> response = authController.logout(authentication);

        verify(authService).logout("test@test.com");
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult()).isNull();
    }

    @Test
    void deleteAccount_usesAuthenticationNameToInvokeService() {
        given(authentication.getName()).willReturn("test@test.com");

        ApiResponse<Void> response = authController.deleteAccount(authentication);

        verify(authService).delete("test@test.com");
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult()).isNull();
    }

    @Test
    void constructor_wiresProvidedAuthServiceForDelegation() {
        AuthService providedService = org.mockito.Mockito.mock(AuthService.class);
        AuthController controller = new AuthController(providedService);
        given(providedService.reissue(any())).willReturn(new AuthResDTO.Login("a", "b"));

        controller.reissue(new AuthReqDTO.Reissue("x"));

        verify(providedService).reissue(any());
    }
}