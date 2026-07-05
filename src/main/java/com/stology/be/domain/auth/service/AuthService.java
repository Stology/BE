package com.stology.be.domain.auth.service;

import com.stology.be.domain.auth.dto.AuthReqDTO;
import com.stology.be.domain.auth.dto.AuthResDTO;
import com.stology.be.domain.auth.exception.AuthException;
import com.stology.be.domain.auth.exception.code.AuthErrorCode;
import com.stology.be.global.security.entity.AuthMember;
import com.stology.be.global.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;

    public AuthResDTO.Login reissue(AuthReqDTO.Reissue request) {
        String refreshToken = request.refreshToken();

        if (refreshToken == null || !jwtUtil.isValid(refreshToken)) {
            throw new AuthException(AuthErrorCode.AUTH_INVALID_REFRESH_TOKEN);
        }

        String email = jwtUtil.getEmail(refreshToken);
        AuthMember member = new AuthMember(null);
        String newAccessToken = jwtUtil.createAccessToken(member);
        String newRefreshToken = jwtUtil.createRefreshToken(member);

        return new AuthResDTO.Login(newAccessToken, newRefreshToken);
    }

    public void logout(String username) {
        // 토큰 블랙리스트 처리 or 세션 무효화
    }

    public void delete(String username) {
        // DB에서 유저 삭제
    }
}

