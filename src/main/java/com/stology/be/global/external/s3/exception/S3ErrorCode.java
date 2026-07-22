package com.stology.be.global.external.s3.exception;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum S3ErrorCode implements BaseErrorCode {

    EMPTY_FILE(HttpStatus.NOT_FOUND, "S3_404_1", "업로드 할 파일이 존재하지 않습니다."),
    S3_IMAGE_FOUND(HttpStatus.NOT_FOUND, "S3_404_2", "존재하지 않는 이미지 경로입니다.??"),
    S3_INVALID_URL(HttpStatus.INTERNAL_SERVER_ERROR, "S3_500_1", "잘못된 S3 URL 형식입니다."),
    S3_REMOVE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "S3_500_2", "S3 이미지 삭제 실패"),
    S3_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "S3_500_3", "S3 이미지 업로드 실패"),
    S3_READ_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "S3_500_3", "S3 이미지 업로드 실패"),
    S3_NO_OBJECT_KEY(HttpStatus.INTERNAL_SERVER_ERROR, "S3_500_3", "S3 이미지 업로드 실패");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
