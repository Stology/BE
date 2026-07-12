package com.stology.be.domain.upload.exception;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import com.stology.be.global.apiPayload.exception.GeneralException;

public class UploadException extends GeneralException {
    public UploadException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
