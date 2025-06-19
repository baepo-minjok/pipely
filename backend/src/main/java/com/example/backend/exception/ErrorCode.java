package com.example.backend.exception;

public enum ErrorCode {
    ERROR_CODE(500,"서버오류"),
    VALIDATION_FAILED(300,"Validation Failed"),;

    private final int status;
    private final String message;

    private ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }
}
