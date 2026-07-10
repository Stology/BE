package com.stology.be.global.security.filter;

import com.stology.be.domain.member.enums.SocialType;
import com.stology.be.global.security.service.CustomUserDetailsService;
import com.stology.be.global.security.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);  // Bearer 뒤의 토큰만 나옴
        if (token == null || !jwtUtil.isValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String uid = jwtUtil.getUid(token);
        SocialType socialType = jwtUtil.getSocialType(token);

        UserDetails member = customUserDetailsService.loadUserByUidAndSocialType(socialType, uid);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                member,
                null,
                member.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
