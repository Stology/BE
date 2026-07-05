package com.stology.be.domain.member.exception.code;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class MemberErrorCodeTest {

    @Test
    void memberNotSupportedSocialProvider_hasExpectedAttributes() {
        MemberErrorCode code = MemberErrorCode.MEMBER_NOT_SUPPORTED_SOCIAL_PROVIDER;

        assertThat(code.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(code.getCode()).isEqualTo("MEMBER400_2");
        assertThat(code.getMessage()).isEqualTo("지원되지 않는 소셜 로그인 유형입니다.");
    }

    @Test
    void everyConstant_hasNonNullAttributes() {
        for (MemberErrorCode code : MemberErrorCode.values()) {
            assertThat(code.getHttpStatus()).isNotNull();
            assertThat(code.getCode()).isNotBlank();
            assertThat(code.getMessage()).isNotBlank();
        }
    }

    @Test
    void codes_areUnique() {
        long distinctCodes = java.util.Arrays.stream(MemberErrorCode.values())
                .map(MemberErrorCode::getCode)
                .distinct()
                .count();

        assertThat(distinctCodes).isEqualTo(MemberErrorCode.values().length);
    }
}