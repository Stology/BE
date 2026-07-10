package com.stology.be.domain.auth.controller;

import com.stology.be.domain.auth.dto.AuthResDTO;
import com.stology.be.domain.auth.dto.TokenPair;
import com.stology.be.domain.auth.exception.AuthException;
import com.stology.be.domain.auth.exception.code.AuthErrorCode;
import com.stology.be.domain.auth.exception.code.AuthSuccessCode;
import com.stology.be.domain.auth.service.AuthService;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.apiPayload.code.GeneralSuccessCode;
import com.stology.be.global.security.entity.AuthMember;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Access Token 재발급
    @PostMapping("/reissue")
    public ApiResponse<AuthResDTO.Reissue> reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Cookie cookie = getRefreshTokenCookie(request);
        TokenPair tokenPair = authService.reissue(cookie.getValue());

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokenPair.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        AuthResDTO.Reissue res = new AuthResDTO.Reissue(tokenPair.accessToken());
        return ApiResponse.onSuccess(AuthSuccessCode.AUTH_REISSUE_SUCCESS, res);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Cookie cookie = getRefreshTokenCookie(request);
        authService.logout(cookie.getValue());
        response.addHeader(HttpHeaders.SET_COOKIE, getExpiredRefreshTokenCookie().toString());

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, null);
    }

    // 회원 탈퇴
    @DeleteMapping("/delete")
    public ApiResponse<Void> deleteAccount(
            HttpServletRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal AuthMember authMember) {
        Cookie cookie = getRefreshTokenCookie(request);
        authService.deleteMember(authMember, cookie.getValue());
        response.addHeader(HttpHeaders.SET_COOKIE, getExpiredRefreshTokenCookie().toString());

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, null);
    }

    private Cookie getRefreshTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        throw new AuthException(AuthErrorCode.AUTH_REFRESH_TOKEN_MISSING);
    }

    private ResponseCookie getExpiredRefreshTokenCookie() {
        ResponseCookie expiredCookie = ResponseCookie.from("refreshToken", null)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .sameSite("None")
                .build();
        return expiredCookie;
    }
}
