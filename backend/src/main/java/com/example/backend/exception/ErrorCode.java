package com.example.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * ErrorCode의 양식
 * 1. 이름: 도메인_상황
 * 2. 내용: http 상태코드, 분류코드(이름_상태코드), 사용자 친화적 메시지
 * 각 도메인 별로 ErrorCode를 분리해주세요.
 */
@Getter
public enum ErrorCode {
    /**
     * GlobalExceptionHanlder에서만 사용되는 ErrorCode
     */
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "UNKNOWN_ERROR_500", "서버 오류입니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED_400", "올바른 값이 아닙니다."),


    /**
     * Auth/User 도메인에서 사용하는 ErrorCode
     */
    USER_REFRESH_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "USER_REFRESH_TOKEN_EXPIRED_400", "만료된 리프레쉬 토큰입니다."),
    USER_REFRESH_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "USER_REFRESH_TOKEN_INVALID_400", "유효하지않은 리프레쉬 토큰입니다."),
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND_400", "유저를 찾을 수 없습니다."),
    USER_DORMANT(HttpStatus.UNAUTHORIZED, "USER_DORMANT_401", "휴면 상태인 유저입니다."),
    USER_WITHDRAWN(HttpStatus.UNAUTHORIZED, "USER_WITHDRAWN_401", "탈퇴한 유저입니다."),
    USER_UNVERIFIED(HttpStatus.UNAUTHORIZED, "USER_UNVERIFIED_401", "이메일 인증을 완료하지 않은 유저입니다."),
    USER_EMAIL_DUPLICATED(HttpStatus.BAD_REQUEST, "USER_EMAIL_DUPLICATED_400", "이미 사용 중인 이메일입니다."),
    USER_PASSWORD_RESET_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "USER_PASSWORD_RESET_TOKEN_INVALID_401", "유효하지 않은 토큰입니다."),
    USER_DORMANT_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "USER_DORMANT_TOKEN_INVALID_401", "유효하지 않은 재활성화 토큰입니다."),
    USER_DORMANT_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "USER_DORMANT_TOKEN_EXPIRED_401", "휴면 재활성화 토큰이 만료되었습니다."),

    /**
     * Auth/Email 도메인에서 사용하는 ErrorCode
     */
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_SEND_FAILED_500", "이메일 발송 실패"),
    EMAIL_VERIFICATION_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "EMAIL_VERFICATION_TOKEN_INVALID_400", "유효하지않은 인증 코드 입니다."),

    /**
     * Jenkins/Info 도메인에서 사용하는 ErrorCode
     */
    JENKINS_INFO_NOT_FOUND(HttpStatus.BAD_REQUEST, "JENKINS_INFO_NOT_FOUND_400", "젠킨스 정보가 존재하지 않습니다"),


    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
