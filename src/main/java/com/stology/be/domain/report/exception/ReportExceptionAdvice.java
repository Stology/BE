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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleReportException(Exception ex) {
        log.error("Report API 처리 중 런타임 에러 발생: ", ex);
        
        GeneralErrorCode code = GeneralErrorCode.INTERNAL_SERVER_ERROR;
        
        return ResponseEntity.status(code.getHttpStatus())
                .body(ApiResponse.onFailure(code, ex.getMessage()));
    }
}
