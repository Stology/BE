package com.stology.be.domain.member.converter;

import com.stology.be.domain.auth.dto.AuthResDTO;
import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.enums.SocialType;
import com.stology.be.global.security.dto.OAuthDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MemberConverterTest {

    @Test
    void toMember_shouldMapOAuthDtoFields() {
        OAuthDTO oauthDto = new OAuthDTO() {
            @Override
            public SocialType getSocialType() {
                return SocialType.KAKAO;
            }

            @Override
            public String getSocialUid() {
                return "kakao-123";
            }

            @Override
            public String getSocialEmail() {
                return "member@test.com";
            }

            @Override
            public String getName() {
                return "홍길동";
            }
        };

        Member member = MemberConverter.toMember(oauthDto);

        assertNotNull(member);
        assertEquals("홍길동", member.getName());
        assertEquals(SocialType.KAKAO, member.getSocialType());
        assertEquals("kakao-123", member.getSocialUid());
        assertEquals("member@test.com", member.getEmail());
    }

    @Test
    void toLogin_shouldWrapAccessToken() {
        AuthResDTO.Login login = MemberConverter.toLogin("access-token");

        assertNotNull(login);
        assertEquals("access-token", login.accessToken());
    }
}
