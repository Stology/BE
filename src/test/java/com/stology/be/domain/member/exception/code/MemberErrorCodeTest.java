package com.stology.be.domain.member.exception.code;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class MemberErrorCodeTest {

    @Test
    void memberNotSupportedSocialProvider_hasExpectedHttpStatusCodeAndMessage() {
        MemberErrorCode errorCode = MemberErrorCode.MEMBER_NOT_SUPPORTED_SOCIAL_PROVIDER;

        assertThat(errorCode.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorCode.getCode()).isEqualTo("MEMBER400_2");
        assertThat(errorCode.getMessage()).isEqualTo("지원되지 않는 소셜 로그인 유형입니다.");
    }

    @Test
    void allErrorCodes_haveUniqueCodeIdentifiers() {
        long distinctCodeCount = Arrays.stream(MemberErrorCode.values())
                .map(MemberErrorCode::getCode)
                .collect(Collectors.toSet())
                .size();

        assertThat(distinctCodeCount).isEqualTo(MemberErrorCode.values().length);
    }

    @Test
    void implementsBaseErrorCode() {
        assertThat(MemberErrorCode.MEMBER_NOT_SUPPORTED_SOCIAL_PROVIDER)
                .isInstanceOf(com.stology.be.global.apiPayload.code.BaseErrorCode.class);
    }
}