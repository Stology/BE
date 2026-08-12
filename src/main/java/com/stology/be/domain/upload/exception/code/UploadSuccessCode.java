package com.stology.be.domain.upload.exception.code;

import com.stology.be.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UploadSuccessCode implements BaseSuccessCode {

    UPLOAD_SUCCESS(
            HttpStatus.OK,
            "UPLOAD200_1",
            "자료 업로드 요청에 성공했습니다."
    ),

    RECENT_FILES_GET_SUCCESS(
            HttpStatus.OK,
            "UPLOAD200_2",
            "최근 업로드 자료 조회에 성공했습니다."
    ),

    MATERIAL_UPDATE_SUCCESS(
            HttpStatus.OK,
            "UPLOAD200_3",
            "자료 정보 수정에 성공했습니다."
    ),

    REANALYZE_REQUEST_SUCCESS(
            HttpStatus.OK,
            "UPLOAD200_4",
            "자료 재분석 요청에 성공했습니다."
    ),

    SUMMARY_GET_SUCCESS(
            HttpStatus.OK,
            "UPLOAD200_5",
            "자료 AI 요약 조회에 성공했습니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}