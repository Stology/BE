package com.stology.be.domain.upload.exception.code;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UploadErrorCode implements BaseErrorCode {

    UPLOAD_FILE_EMPTY(
            HttpStatus.BAD_REQUEST,
            "UPLOAD400_1",
            "업로드할 파일이 존재하지 않습니다."
    ),

    UPLOAD_FILE_EXTENSION_INVALID(
            HttpStatus.BAD_REQUEST,
            "UPLOAD400_2",
            "Markdown(.md) 파일만 업로드할 수 있습니다."
    ),

    UPLOAD_FILE_ENCODING_INVALID(
            HttpStatus.BAD_REQUEST,
            "UPLOAD400_3",
            "올바른 UTF-8 형식의 Markdown 파일이 아닙니다."
    ),

    UPLOAD_FILE_READ_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "UPLOAD500_1",
            "Markdown 파일을 읽는 중 오류가 발생했습니다."
    ),

    UPLOAD_MEMBER_NOT_IN_STUDY(
            HttpStatus.FORBIDDEN,
            "UPLOAD403_1",
            "해당 스터디에 참여한 회원만 자료를 업로드할 수 있습니다."
    ),

    UPLOAD_MEMBER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "UPLOAD404_1",
            "업로드 회원 정보를 찾을 수 없습니다."
    ),

    UPLOAD_S3_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "UPLOAD500_2",
            "파일 저장소에 파일을 업로드하는 중 오류가 발생했습니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}