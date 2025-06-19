
package com.example.backend.exception;


import lombok.Getter;

import java.time.Instant;


@Getter
public class BaseResponse<T> {
    private boolean success;
    private T data;
    private ErrorInfo error;

    // private 생성자
    private BaseResponse(boolean success, T data, ErrorInfo error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    // 성공 응답 생성
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, data, null);
    }

    // 실패 응답 생성: ErrorCode enum과 path, 상세 메시지 등을 받아 ErrorInfo 생성
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String path) {
        ErrorInfo errorInfo = ErrorInfo.builder()
                .timestamp(Instant.now())
                .status(errorCode.getStatus())
                .message(errorCode.getMessage())
                .path(path)
                .build();
        return new BaseResponse<>(false, null, errorInfo);
    }

}