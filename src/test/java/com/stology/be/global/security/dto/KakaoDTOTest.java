package com.stology.be.global.security.dto;

import com.stology.be.domain.member.enums.SocialType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KakaoDTOTest {

    @Test
    void implementsOAuthDTOContractWithKakaoValues() {
        KakaoDTO dto = new KakaoDTO("uid-1", "test@kakao.com", "tester");

        assertThat(dto).isInstanceOf(OAuthDTO.class);
        assertThat(dto.getSocialType()).isEqualTo(SocialType.KAKAO);
        assertThat(dto.getSocialUid()).isEqualTo("uid-1");
        assertThat(dto.getSocialEmail()).isEqualTo("test@kakao.com");
        assertThat(dto.getName()).isEqualTo("tester");
    }

    @Test
    void getSocialType_alwaysReturnsKakaoRegardlessOfInput() {
        KakaoDTO dto = new KakaoDTO(null, null, null);

        assertThat(dto.getSocialType()).isEqualTo(SocialType.KAKAO);
    }
}