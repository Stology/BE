package com.stology.be.domain.auth.service;

import com.stology.be.domain.auth.dto.TokenPair;
import com.stology.be.domain.auth.exception.AuthException;
import com.stology.be.domain.auth.exception.code.AuthErrorCode;
import com.stology.be.domain.auth.repository.RefreshTokenRepository;
import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.exception.MemberException;
import com.stology.be.domain.member.exception.code.MemberErrorCode;
import com.stology.be.domain.member.repository.MemberRepository;
import com.stology.be.global.security.entity.AuthMember;
import com.stology.be.global.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;

    public TokenPair reissue(String refreshToken) {
        if (refreshToken == null || !jwtUtil.isValid(refreshToken)) {
            System.out.println("refreshToken: " + refreshToken);
            System.out.println("isValid: " + jwtUtil.isValid(refreshToken));
            throw new AuthException(AuthErrorCode.AUTH_INVALID_REFRESH_TOKEN);
        }

        String uid = jwtUtil.getUid(refreshToken);
        String savedToken = refreshTokenRepository.findBySocialUid(uid)
                .orElseThrow(() -> new AuthException(AuthErrorCode.AUTH_INVALID_REFRESH_TOKEN));
        if (!refreshToken.equals(savedToken)) {
            throw new AuthException(AuthErrorCode.AUTH_INVALID_REFRESH_TOKEN);
        }

        refreshTokenRepository.delete(uid);

        AuthMember member = new AuthMember(
                memberRepository.findBySocialUid(uid)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND
                )));
        String newAccessToken = jwtUtil.createAccessToken(member);
        String newRefreshToken = jwtUtil.createRefreshToken(member);
        refreshTokenRepository.save(uid, newRefreshToken, 7 * 24 * 60 * 60);

        return new TokenPair(newAccessToken, newRefreshToken);
    }

    public void logout(String refreshToken) {
        String uid = jwtUtil.getUid(refreshToken);
        refreshTokenRepository.delete(uid);
    }

    public void deleteMember(AuthMember authMember, String refreshToken) {
        String uid = jwtUtil.getUid(refreshToken);
        refreshTokenRepository.delete(uid);

        Member member = authMember.getMember();
        member.softDelete();
    }
}

