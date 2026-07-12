package com.stology.be.domain.study.exception;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import com.stology.be.global.apiPayload.exception.GeneralException;

public class StudyException extends GeneralException {
    public StudyException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
