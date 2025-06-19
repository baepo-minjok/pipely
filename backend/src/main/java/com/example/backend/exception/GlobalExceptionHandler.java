package com.example.backend.exception;

import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public GlobalExceptionHandler() {
    }

    // @Valid 유효성 검증 실패 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        // FieldError들을 조합: 예: "name: 필수값 누락, age: 범위 벗어남"
        String detailMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> formatFieldError(fieldError))
                .collect(Collectors.joining(", "));

        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
        // path 정보
        String path = request.getRequestURI();
        BaseResponse<Object> body = BaseResponse.error(errorCode, path);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    private String formatFieldError(FieldError fieldError) {
        // 기본 메시지: field 이름 + defaultMessage
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    @ExceptionHandler({CustomException.class})
    public ResponseEntity<BaseResponse<String>> handleCustomException(CustomException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();

        // path 정보
        String path = request.getRequestURI();
        return ResponseEntity.badRequest().body(BaseResponse.error(errorCode,path));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<BaseResponse<String>> handleException(Exception ex, HttpServletRequest request) {
        log.error(ex.getMessage());
        ErrorCode code = ErrorCode.ERROR_CODE;

        // path 정보
        String path = request.getRequestURI();

        return ResponseEntity.badRequest().body(BaseResponse.error(code, path));
    }
}
