package com.stology.be.global.security.handler;

import com.stology.be.domain.auth.repository.RefreshTokenRepository;
import com.stology.be.global.security.entity.AuthMember;
import com.stology.be.global.security.entity.OAuthMember;
import com.stology.be.global.security.service.OAuthRedirectService;
import com.stology.be.global.security.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuthRedirectService oAuthRedirectService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuthMember member = (OAuthMember) authentication.getPrincipal();
        AuthMember authMember = new AuthMember(member.getMember());

        String refreshToken = jwtUtil.createRefreshToken(authMember);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        refreshTokenRepository.save(member.getName(), refreshToken, 7 * 24 * 60 * 60);

        String successRedirectUrl = resolveSuccessRedirectUrl(request);
        response.addHeader(HttpHeaders.SET_COOKIE, expireRedirectCookie().toString());
        response.sendRedirect(successRedirectUrl);
    }

    private String resolveSuccessRedirectUrl(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return oAuthRedirectService.resolveRedirectUrl(null);
        }

        for (Cookie cookie : cookies) {
            if (OAuthRedirectService.REDIRECT_COOKIE_NAME.equals(cookie.getName())) {
                return oAuthRedirectService.resolveRedirectUrl(
                        oAuthRedirectService.decode(cookie.getValue())
                );
            }
        }

        return oAuthRedirectService.resolveRedirectUrl(null);
    }

    private ResponseCookie expireRedirectCookie() {
        return ResponseCookie.from(OAuthRedirectService.REDIRECT_COOKIE_NAME, null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
    }
}
