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
    void toMember_mapsFieldsFromOAuthDTO() {
        OAuthDTO dto = new KakaoDTO("social-uid-1", "user@example.com", "Kakao Name");

        Member member = MemberConverter.toMember(dto);

        assertThat(member.getName()).isEqualTo("Kakao Name");
        assertThat(member.getSocialType()).isEqualTo(SocialType.KAKAO);
        assertThat(member.getSocialUid()).isEqualTo("social-uid-1");
        assertThat(member.getEmail()).isEqualTo("user@example.com");
        assertThat(member.getId()).isNull();
    }

    @Test
    void toLogin_wrapsTokensIntoLoginResponse() {
        AuthResDTO.Login login = MemberConverter.toLogin("access-tok", "refresh-tok");

        assertThat(login.accessToken()).isEqualTo("access-tok");
        assertThat(login.refreshToken()).isEqualTo("refresh-tok");
    }
}