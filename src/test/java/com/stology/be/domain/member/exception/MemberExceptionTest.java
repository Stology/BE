package com.stology.be.domain.member.exception;

import com.stology.be.domain.member.exception.code.MemberErrorCode;
import com.stology.be.global.apiPayload.exception.GeneralException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberExceptionTest {

    @Test
    void constructor_setsProvidedErrorCode() {
        MemberException exception = new MemberException(MemberErrorCode.MEMBER_NOT_SUPPORTED_SOCIAL_PROVIDER);

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_SUPPORTED_SOCIAL_PROVIDER);
    }

    @Test
    void isInstanceOfGeneralException() {
        MemberException exception = new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);

        assertThat(exception).isInstanceOf(GeneralException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}