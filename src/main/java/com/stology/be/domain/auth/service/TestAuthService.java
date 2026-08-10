package com.stology.be.domain.auth.service;

import com.stology.be.domain.auth.dto.TokenDTO;
import com.stology.be.domain.auth.exception.AuthException;
import com.stology.be.domain.auth.exception.code.AuthErrorCode;
import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.enums.SocialType;
import com.stology.be.domain.member.repository.MemberRepository;
import com.stology.be.global.security.entity.AuthMember;
import com.stology.be.global.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TestAuthService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    @Value("${test.secret-code}")
    private String testSecretCode;

    @Transactional
    public TokenDTO loginAsTestAccount(String secretCode) {
        // 1. 보안을 위한 단순 암호/코드 검증
        if (!testSecretCode.equals(secretCode)) {
            throw new AuthException(AuthErrorCode.AUTH_UNAUTHORIZED_SECRET_TOKEN);
        }

        // 2. 테스트 유저가 DB에 없으면 자동 생성
        Member testMember = memberRepository.findByEmail("test@test.com")
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .email("test@test.com")
                        .name("testAccount")
                        .socialUid("4976922864")
                        .socialType(SocialType.KAKAO)
                        .build()));

        AuthMember authMember = new AuthMember(testMember);

        // 3. 기존 카카오 로그인 성공 시 발급하는 방식과 동일하게 JWT 생성
        String accessToken = jwtUtil.createAccessToken(authMember);
        String refreshToken = jwtUtil.createRefreshToken(authMember);

        return new TokenDTO(authMember.getMemberId(), accessToken, refreshToken);
    }
}