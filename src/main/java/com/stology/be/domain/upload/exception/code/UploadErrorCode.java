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
    UPLOAD_CONTENT_EMPTY(
            HttpStatus.BAD_REQUEST,
            "UPLOAD400_1",
            "업로드할 내용이 존재하지 않습니다."
    ),

    UPLOAD_FILE_EXTENSION_INVALID(
            HttpStatus.BAD_REQUEST,
            "UPLOAD400_2",
            "Markdown(.md), txt(.txt) 파일만 업로드할 수 있습니다."
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
    ),
    AI_RESULT_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
            "SUMMARY_SAVE400_1",
                    "AI 응답 결과가 없습니다."
    ),

    AI_SUMMARY_EMPTY(
            HttpStatus.BAD_REQUEST,
            "SUMMARY_SAVE400_2",
                    "AI 요약 결과가 비어 있습니다."
    ),

    AI_SUMMARY_NOT_COMPLETE(
            HttpStatus.BAD_REQUEST,
            "SUMMARY_SAVE400_2",
            "AI 요약 결과를 만드는 중입니다."
    ),

    AI_SELECTED_NODE_LIMIT_EXCEEDED(
            HttpStatus.BAD_REQUEST,
            "SUMMARY_SAVE400_3",
                    "AI가 선택할 수 있는 노드는 최대 3개입니다."
    ),

    STUDY_MATERIAL_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "SUMMARY_SAVE404_1",
                    "존재하지 않는 스터디 자료입니다."
    ),

    STUDY_NODE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "SUMMARY_SAVE404_2",
                    "존재하지 않는 스터디 노드가 포함되어 있습니다."
    ),
    NO_GRANDTED_FOR_STUDY_MATERIAL(
            HttpStatus.NOT_FOUND,
            "SUMMARY_SAVE404_2",
            "당신의 스터디 자료가 아닙니다"
    ),
    MATERIAL_UPDATE_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "DATA_UPDATE_409_1",
            "AI 추출중임으로 업데이트 불가합니다."
    ),

    STUDY_NODE_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "SUMMARY_SAVE400_4",
                    "업로드 자료와 다른 스터디의 노드를 후보로 등록할 수 없습니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}