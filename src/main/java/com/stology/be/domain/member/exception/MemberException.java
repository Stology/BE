package com.stology.be.domain.member.exception;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import com.stology.be.global.apiPayload.exception.GeneralException;

public class MemberException extends GeneralException {
    public MemberException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
