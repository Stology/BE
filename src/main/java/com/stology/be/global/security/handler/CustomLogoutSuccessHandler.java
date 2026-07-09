package com.stology.be.global.security.handler;

import com.stology.be.domain.auth.exception.AuthException;
import com.stology.be.domain.auth.exception.code.AuthErrorCode;
import com.stology.be.domain.auth.exception.code.AuthSuccessCode;
import com.stology.be.domain.auth.repository.RefreshTokenRepository;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.apiPayload.code.BaseSuccessCode;
import com.stology.be.global.security.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            @Nullable Authentication authentication
    ) throws IOException, ServletException {
        ObjectMapper objectMapper = new ObjectMapper();
        BaseSuccessCode code = AuthSuccessCode.AUTH_LOGOUT_SUCCESS;

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code.getHttpStatus().value());

        Cookie cookie = getRefreshTokenCookie(request);
        if (jwtUtil.isValid(cookie.getValue())) {
            String uid = jwtUtil.getUid(cookie.getValue());
            refreshTokenRepository.delete(uid);
        }
        response.addHeader(HttpHeaders.SET_COOKIE, getExpiredRefreshTokenCookie().toString());

        ApiResponse<Void> responseBody = ApiResponse.onSuccess(code, null);

        objectMapper.writeValue(response.getOutputStream(), responseBody);
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