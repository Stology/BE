package com.stology.be.domain.auth.controller;

import com.stology.be.domain.auth.dto.AuthReqDTO;
import com.stology.be.domain.auth.dto.AuthResDTO;
import com.stology.be.domain.auth.dto.TokenDTO;
import com.stology.be.domain.auth.exception.code.AuthSuccessCode;
import com.stology.be.domain.auth.service.TestAuthService;
import com.stology.be.global.apiPayload.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TestAuthController {

    private final TestAuthService testAuthService;

    @PostMapping("/test") // POST /oauth2/authorization/test
    public ApiResponse<AuthResDTO.Login> loginForReviewer(
            @RequestBody AuthReqDTO.TestRequest request,
            HttpServletResponse response
    ) {
        // 1. 테스트 계정 전용 토큰 생성 및 토큰 발급
        TokenDTO res = testAuthService.loginAsTestAccount(request.secretCode());

        // 2. Refresh Token을 Cookie로 설정 (기존 소셜 로그인 응답 스펙과 동일하게 맞춤)
        ResponseCookie cookie = ResponseCookie.from("refreshToken", res.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 3. Access Token은 Response Body로 반환
        return ApiResponse.onSuccess(
                AuthSuccessCode.AUTH_TEST_LOGIN_SUCCESS,
                new AuthResDTO.Login(res.accessToken())
        );
    }
}
