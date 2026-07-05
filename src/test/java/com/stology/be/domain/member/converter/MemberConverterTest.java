package com.stology.be.domain.member.converter;

import com.stology.be.domain.auth.dto.AuthResDTO;
import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.enums.SocialType;
import com.stology.be.global.security.dto.KakaoDTO;
import com.stology.be.global.security.dto.OAuthDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberConverterTest {

    @Test
    void toMember_mapsAllFieldsFromOAuthDTO() {
        OAuthDTO dto = new KakaoDTO("uid-123", "test@kakao.com", "tester");

        Member member = MemberConverter.toMember(dto);

        assertThat(member.getName()).isEqualTo("tester");
        assertThat(member.getSocialType()).isEqualTo(SocialType.KAKAO);
        assertThat(member.getSocialUid()).isEqualTo("uid-123");
        assertThat(member.getEmail()).isEqualTo("test@kakao.com");
        assertThat(member.getId()).isNull();
    }

    @Test
    void toLogin_wrapsTokensIntoLoginResponse() {
        AuthResDTO.Login login = MemberConverter.toLogin("access-token", "refresh-token");

        assertThat(login.accessToken()).isEqualTo("access-token");
        assertThat(login.refreshToken()).isEqualTo("refresh-token");
    }
}