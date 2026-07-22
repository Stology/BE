package com.stology.be.domain.inquiry.exception;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InquiryErrorCode implements BaseErrorCode {

    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "INQUIRY404_1", "해당 질문을 찾을 수 없습니다."),
    INQUIRY_FORBIDDEN(HttpStatus.FORBIDDEN, "INQUIRY403_1", "본인이 작성한 질문만 수정 및 삭제할 수 있습니다."),
    INQUIRY_TITLE_INVALID(HttpStatus.BAD_REQUEST, "INQUIRY400_1", "제목은 1자 이상 50자 이하로 입력해주세요."),
    INQUIRY_BODY_INVALID(HttpStatus.BAD_REQUEST, "INQUIRY400_2", "본문은 1자 이상 1000자 이하로 입력해주세요."),
    REPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "INQUIRY404_2", "해당 답글을 찾을 수 없습니다."),
    INQUIRY_GONE(HttpStatus.GONE, "INQUIRY410_1", "이미 삭제된 질문입니다. 목록을 새로고침해주세요."),
    REPLY_GONE(HttpStatus.GONE, "INQUIRY410_2", "이미 삭제된 답글입니다. 목록을 새로고침해주세요."),
    REPLY_FORBIDDEN(HttpStatus.FORBIDDEN, "INQUIRY403_2", "본인이 작성한 답글만 수정 및 삭제할 수 있습니다."),
    REPLY_BODY_INVALID(HttpStatus.BAD_REQUEST, "INQUIRY400_3", "답글 내용을 입력해주세요."),
    STUDY_MEMBER_FORBIDDEN(HttpStatus.FORBIDDEN, "INQUIRY403_3", "스터디 멤버만 이용할 수 있습니다."),
    STUDY_ENDED(HttpStatus.BAD_REQUEST, "INQUIRY400_4", "종료된 스터디에서는 이용할 수 없습니다."),
    IMAGE_FILE_INVALID(HttpStatus.BAD_REQUEST, "INQUIRY400_5", "이미지 파일만 업로드할 수 있습니다."),
    IMAGE_URL_INVALID(HttpStatus.BAD_REQUEST, "INQUIRY400_6", "업로드 API가 발급한 이미지 URL만 사용할 수 있습니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "INQUIRY500_1", "이미지 업로드에 실패했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
