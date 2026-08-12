package com.stology.be.domain.home.exception;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import com.stology.be.global.apiPayload.exception.GeneralException;

public class HomeException extends GeneralException {
    public HomeException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
