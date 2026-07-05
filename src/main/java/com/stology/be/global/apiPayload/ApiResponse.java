package com.stology.be.global.apiPayload;

import com.stology.be.global.apiPayload.code.BaseErrorCode;
import com.stology.be.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean isSuccess;
    private String code;
    private String message;
    private T result;
    private T errorDetail;

    public static <T> ApiResponse<T> onSuccess(BaseSuccessCode code, T result) {
        return new ApiResponse<>(true, code.getCode(), code.getMessage(), result, null);
    }

    public static <T> ApiResponse<T> onFailure(BaseErrorCode code, T errorDetail) {
        return new ApiResponse<>(false, code.getCode(), code.getMessage(), null, errorDetail);
    }
}
