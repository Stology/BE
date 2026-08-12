package com.stology.be.domain.report.exception;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReportErrorCode implements BaseErrorCode {
    
    STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_404_1", "해당 스터디를 찾을 수 없어 리포트를 조회/생성할 수 없습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_404_2", "해당 주차의 리포트가 존재하지 않습니다."),
    REPORT_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "REPORT_500_1", "AI 리포트 생성 중 통신 오류가 발생했습니다."),
    START_DATE_NOT_SET(HttpStatus.BAD_REQUEST, "REPORT_400_1", "스터디 시작일이 설정되지 않아 리포트를 생성할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
