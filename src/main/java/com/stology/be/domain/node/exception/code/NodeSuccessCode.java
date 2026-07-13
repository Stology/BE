package com.stology.be.domain.node.exception.code;

import com.stology.be.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NodeSuccessCode implements BaseSuccessCode {

    // study
    GET_SUCCESS(HttpStatus.OK, "STUDY200_1", "성공적으로 조회했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
