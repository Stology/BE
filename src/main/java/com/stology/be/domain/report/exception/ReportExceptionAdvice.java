package com.stology.be.domain.report.exception;

import com.stology.be.domain.report.controller.ReportController;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.apiPayload.code.GeneralErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(assignableTypes = {ReportController.class})
public class ReportExceptionAdvice {

    @ExceptionHandler(ReportException.class)
    public ResponseEntity<ApiResponse<Void>> handleReportException(ReportException e) {
        log.warn("Report API 비즈니스 예외 발생: {}", e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.onFailure(e.getErrorCode(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception ex) {
        log.error("Report API 처리 중 런타임 에러 발생: ", ex);
        
        GeneralErrorCode code = GeneralErrorCode.INTERNAL_SERVER_ERROR;
        
        // 자세한 에러 원인 출력을 위해 ex.toString()을 사용합니다.
        return ResponseEntity.status(code.getHttpStatus())
                .body(ApiResponse.onFailure(code, ex.toString()));
    }
}
