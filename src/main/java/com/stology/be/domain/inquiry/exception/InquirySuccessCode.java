package com.stology.be.domain.inquiry.exception;

import com.stology.be.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InquirySuccessCode implements BaseSuccessCode {

    // ===== 질문(Question) =====
    GET_INQUIRIES(HttpStatus.OK, "INQUIRY200_1", "질문 목록 조회 성공"),
    GET_INQUIRY_DETAIL(HttpStatus.OK, "INQUIRY200_2", "질문 상세 조회 성공"),
    WRITE_INQUIRY(HttpStatus.OK, "INQUIRY200_3", "질문이 등록되었습니다."),
    UPDATE_INQUIRY(HttpStatus.OK, "INQUIRY200_4", "질문이 수정되었습니다."),
    DELETE_INQUIRY(HttpStatus.OK, "INQUIRY200_5", "질문이 삭제되었습니다."),

    // ===== 답변(Answer) =====
    WRITE_REPLY(HttpStatus.OK, "INQUIRY200_6", "답글이 등록되었습니다."),
    UPDATE_REPLY(HttpStatus.OK, "INQUIRY200_7", "답글이 수정되었습니다."),
    DELETE_REPLY(HttpStatus.OK, "INQUIRY200_8", "답글이 삭제되었습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
