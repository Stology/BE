package com.stology.be.global.security.dto;

import com.stology.be.domain.member.enums.SocialType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KakaoDTOTest {

    @Test
    void getters_returnConstructorValues() {
        KakaoDTO dto = new KakaoDTO("12345", "test@example.com", "TestUser");

        assertThat(dto.getSocialUid()).isEqualTo("12345");
        assertThat(dto.getSocialEmail()).isEqualTo("test@example.com");
        assertThat(dto.getName()).isEqualTo("TestUser");
    }

    @Test
    void getSocialType_isAlwaysKakao() {
        KakaoDTO dto = new KakaoDTO("uid", "email@test.com", "name");

        assertThat(dto.getSocialType()).isEqualTo(SocialType.KAKAO);
    }

    @Test
    void implementsOAuthDTO() {
        KakaoDTO dto = new KakaoDTO("uid", "email@test.com", "name");

        assertThat(dto).isInstanceOf(OAuthDTO.class);
    }
}