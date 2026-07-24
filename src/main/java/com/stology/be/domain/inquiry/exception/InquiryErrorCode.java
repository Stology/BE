package com.stology.be.domain.inquiry.exception;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InquiryErrorCode implements BaseErrorCode {

    // ===== 질문(Question) =====
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "INQUIRY404_1", "해당 질문을 찾을 수 없습니다."),
    INQUIRY_FORBIDDEN(HttpStatus.FORBIDDEN, "INQUIRY403_1", "본인이 작성한 질문만 수정 및 삭제할 수 있습니다."),
    INQUIRY_TITLE_INVALID(HttpStatus.BAD_REQUEST, "INQUIRY400_1", "제목은 1자 이상 50자 이하로 입력해주세요."),
    INQUIRY_BODY_INVALID(HttpStatus.BAD_REQUEST, "INQUIRY400_2", "본문은 1자 이상 1000자 이하로 입력해주세요."),

    // ===== 답변(Answer) =====
    REPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "INQUIRY404_2", "해당 답글을 찾을 수 없습니다."),
    REPLY_FORBIDDEN(HttpStatus.FORBIDDEN, "INQUIRY403_2", "본인이 작성한 답글만 수정 및 삭제할 수 있습니다."),
    REPLY_BODY_INVALID(HttpStatus.BAD_REQUEST, "INQUIRY400_3", "답글은 1자 이상 1000자 이하로 입력해주세요."),

    // ===== 이미지(Image) =====
    IMAGE_FILE_INVALID(HttpStatus.BAD_REQUEST, "INQUIRY400_4", "이미지 파일만 업로드할 수 있습니다."),
    IMAGE_URL_INVALID(HttpStatus.BAD_REQUEST, "INQUIRY400_5", "본문이 존재하지 않는 이미지를 참조하고 있습니다."),
    IMAGE_TOKEN_MISMATCH(HttpStatus.BAD_REQUEST, "INQUIRY400_6", "본문의 이미지 자리표시자([[img:new:K]])와 첨부한 이미지 개수가 일치하지 않습니다."),
    IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "INQUIRY400_7", "이미지는 최대 5개까지 첨부할 수 있습니다."),

    // ===== 스터디/공통 =====
    STUDY_MEMBER_FORBIDDEN(HttpStatus.FORBIDDEN, "INQUIRY403_3", "스터디 멤버만 이용할 수 있습니다."),
    STUDY_ENDED(HttpStatus.BAD_REQUEST, "INQUIRY400_8", "종료된 스터디에서는 이용할 수 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
