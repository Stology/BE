package com.stology.be.domain.node.exception;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import com.stology.be.global.apiPayload.exception.GeneralException;

public class NodeException extends GeneralException {
    public NodeException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
