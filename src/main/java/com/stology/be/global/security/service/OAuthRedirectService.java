package com.stology.be.global.security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Component
public class OAuthRedirectService {

    public static final String REDIRECT_COOKIE_NAME = "oauthRedirectUrl";

    private final String defaultRedirectUrl;
    private final List<String> allowedRedirectUrls;

    public OAuthRedirectService(
            @Value("${app.oauth.success-redirect-url:https://stology.vercel.app/}") String defaultRedirectUrl,
            @Value("${app.oauth.allowed-redirect-urls:https://stology.vercel.app/,http://localhost:5173/}") String[] allowedRedirectUrls
    ) {
        this.defaultRedirectUrl = normalize(defaultRedirectUrl);
        this.allowedRedirectUrls = Arrays.stream(allowedRedirectUrls)
                .map(this::normalize)
                .toList();
    }

    public String resolveRedirectUrl(String requestedRedirectUrl) {
        String normalized = normalize(requestedRedirectUrl);
        if (normalized != null && allowedRedirectUrls.contains(normalized)) {
            return normalized;
        }
        return defaultRedirectUrl;
    }

    public String encode(String redirectUrl) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(redirectUrl.getBytes(StandardCharsets.UTF_8));
    }

    public String decode(String encodedRedirectUrl) {
        if (encodedRedirectUrl == null || encodedRedirectUrl.isBlank()) {
            return null;
        }
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encodedRedirectUrl);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String normalize(String redirectUrl) {
        if (redirectUrl == null || redirectUrl.isBlank()) {
            return null;
        }
        String trimmed = redirectUrl.trim();
        return trimmed.endsWith("/") ? trimmed : trimmed + "/";
    }
}
