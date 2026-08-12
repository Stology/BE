package com.stology.be.domain.report.exception;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;

@Getter
public class ReportException extends RuntimeException {
    private final BaseErrorCode errorCode;

    public ReportException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
