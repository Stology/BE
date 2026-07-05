package com.stology.be.domain.auth.exception;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import com.stology.be.global.apiPayload.exception.GeneralException;

public class AuthException extends GeneralException {
    public AuthException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
