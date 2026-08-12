package com.stology.be.global.external.s3.exception;

import com.stology.be.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum S3SuccessCode implements BaseSuccessCode {

    S3_UPLOAD_SUCCESSFUL(HttpStatus.OK, "S3_200_1", "사진 업로드 성공"),
    S3_REMOVE_SUCCESSFUL(HttpStatus.OK, "S3_200_2", "사진 삭제 성공");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;



}
