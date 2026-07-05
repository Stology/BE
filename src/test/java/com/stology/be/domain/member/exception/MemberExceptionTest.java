package com.stology.be.domain.member.exception;

import com.stology.be.domain.member.exception.code.MemberErrorCode;
import com.stology.be.global.apiPayload.exception.GeneralException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberExceptionTest {

    @Test
    void constructor_storesErrorCode() {
        MemberException exception = new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    void isInstanceOfGeneralException() {
        MemberException exception = new MemberException(MemberErrorCode.MEMBER_BAD_REQUEST);

        assertThat(exception).isInstanceOf(GeneralException.class);
    }
}