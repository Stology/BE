package com.stology.be.domain.member.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SocialTypeTest {

    @Test
    void kakao_isDefinedSocialType() {
        assertThat(SocialType.valueOf("KAKAO")).isEqualTo(SocialType.KAKAO);
    }

    @Test
    void values_containsOnlyKakao() {
        assertThat(SocialType.values()).containsExactly(SocialType.KAKAO);
    }

    @Test
    void valueOf_unknownProvider_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> SocialType.valueOf("NAVER"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}