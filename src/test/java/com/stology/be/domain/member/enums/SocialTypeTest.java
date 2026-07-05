package com.stology.be.domain.member.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SocialTypeTest {

    @Test
    void values_containsExactlyKakao() {
        assertThat(SocialType.values()).containsExactly(SocialType.KAKAO);
    }

    @Test
    void valueOf_kakao_returnsKakaoConstant() {
        assertThat(SocialType.valueOf("KAKAO")).isEqualTo(SocialType.KAKAO);
    }

    @Test
    void valueOf_unsupportedProvider_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> SocialType.valueOf("GOOGLE"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}