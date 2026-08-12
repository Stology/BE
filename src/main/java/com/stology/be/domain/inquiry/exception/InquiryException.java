package com.stology.be.domain.inquiry.exception;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import com.stology.be.global.apiPayload.exception.GeneralException;

public class InquiryException extends GeneralException {

    public InquiryException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
