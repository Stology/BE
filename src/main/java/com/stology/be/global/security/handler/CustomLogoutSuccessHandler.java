package com.stology.be.global.security.handler;

import com.stology.be.domain.auth.exception.AuthException;
import com.stology.be.domain.auth.exception.code.AuthErrorCode;
import com.stology.be.domain.auth.exception.code.AuthSuccessCode;
import com.stology.be.domain.auth.repository.BlacklistTokenRepository;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistTokenRepository blacklistTokenRepository;

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
        blacklistAccessToken(resolveAccessToken(request));
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

    private String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void blacklistAccessToken(String accessToken) {
        if (accessToken == null || !jwtUtil.isValid(accessToken)) {
            return;
        }

        long ttlSeconds = jwtUtil.getRemainingExpirationSeconds(accessToken);
        if (ttlSeconds > 0) {
            blacklistTokenRepository.save(accessToken, ttlSeconds);
        }
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
