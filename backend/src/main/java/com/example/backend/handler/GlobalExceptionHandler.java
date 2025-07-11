package com.example.backend.handler;

import com.example.backend.exception.BaseResponse;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public GlobalExceptionHandler() {
    }

    // @Valid 유효성 검증 실패 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
        String path = request.getRequestURI();
        String method = request.getMethod();
        String query = request.getQueryString(); // null일 수 있음

        // FieldError 목록에서 상세 메시지 조합
        String detailMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        // 로깅: WARN 레벨로 남기되, 어느 요청에서 무슨 필드 오류가 났는지 구체적으로 기록
        if (query != null) {
            log.warn("Validation failed [{} {}?{}]: {}", method, path, query, detailMessage);
        } else {
            log.warn("Validation failed [{} {}]: {}", method, path, detailMessage);
        }

        return ResponseEntity
                .status(errorCode.getHttpStatus().value())
                .body(BaseResponse.error(errorCode, path));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<BaseResponse<String>> handleCustomException(CustomException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        String path = request.getRequestURI();
        String method = request.getMethod();
        String query = request.getQueryString();
        String message = ex.getMessage();

        // 어떤 비즈니스 로직에서 발생했는지 로그에 남김
        if (query != null) {
            log.warn("Business exception [{} {}?{}]: code={}, message={}", method, path, query, errorCode.name(), message);
        } else {
            log.warn("Business exception [{} {}]: code={}, message={}", method, path, errorCode.name(), message);
        }

        return ResponseEntity
                .status(errorCode.getHttpStatus().value())
                .body(BaseResponse.error(errorCode, path));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<?>> handleMissingParams(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {

        String path = request.getRequestURI();
        String method = request.getMethod();
        String query = request.getQueryString();

        if (query != null) {
            log.error("MissingServletRequestParameter exception [{} {}?{}]: {}", method, path, query, ex.getMessage(), ex);
        } else {
            log.error("MissingServletRequestParameter exception [{} {}]: {}", method, path, ex.getMessage(), ex);
        }

        ErrorCode code = ErrorCode.MISSING_PARAMETER;

        return ResponseEntity
                .status(code.getHttpStatus().value())
                .body(BaseResponse.error(code, path));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BaseResponse<String>> handleUsernameNotFoundException(Exception ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String query = request.getQueryString();

        ErrorCode code = ErrorCode.USER_LOGIN_FAILED;

        return ResponseEntity
                .status(code.getHttpStatus())
                .body(BaseResponse.error(code, path));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<String>> handleAuthenticationException(Exception ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String query = request.getQueryString();

        ErrorCode code = ErrorCode.USER_LOGIN_FAILED;

        return ResponseEntity
                .status(code.getHttpStatus().value())
                .body(BaseResponse.error(code, path));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<BaseResponse<String>> handleIOException(Exception ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String query = request.getQueryString();

        ErrorCode code = ErrorCode.IOEXCEPTION;

        return ResponseEntity
                .status(code.getHttpStatus().value())
                .body(BaseResponse.error(code, path));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<BaseResponse<String>> handleAuthorizationDeniedException(Exception ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String query = request.getQueryString();

        ErrorCode code = ErrorCode.METHOD_UNAUTHORIZED;

        return ResponseEntity
                .status(code.getHttpStatus().value())
                .body(BaseResponse.error(code, path));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<String>> handleException(Exception ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String query = request.getQueryString();

        // 예기치 않은 예외: 스택 트레이스를 남겨야 원인 파악 가능
        if (query != null) {
            log.error("Unhandled exception [{} {}?{}]: {}", method, path, query, ex.getMessage(), ex);
        } else {
            log.error("Unhandled exception [{} {}]: {}", method, path, ex.getMessage(), ex);
        }

        ErrorCode code = ErrorCode.UNKNOWN_ERROR;

        return ResponseEntity
                .status(code.getHttpStatus().value())
                .body(BaseResponse.error(code, path));
    }

}
