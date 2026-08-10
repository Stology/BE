package com.stology.be.global.security.controller;

import com.stology.be.global.security.service.OAuthRedirectService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class OAuthRedirectController {

    private final OAuthRedirectService oAuthRedirectService;

    @GetMapping("/api/auth/oauth2/kakao")
    public void redirectToKakao(
            @RequestParam(required = false) String redirectUrl,
            HttpServletResponse response
    ) throws IOException {
        String targetRedirectUrl = oAuthRedirectService.resolveRedirectUrl(redirectUrl);
        ResponseCookie redirectCookie = ResponseCookie.from(
                        OAuthRedirectService.REDIRECT_COOKIE_NAME,
                        oAuthRedirectService.encode(targetRedirectUrl)
                )
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(180)
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, redirectCookie.toString());
        response.sendRedirect("/oauth2/authorization/kakao");
    }
}
