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
     * Mustache에서 사용하는 ErrorCode
     */
    MUSTACHE_FILE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "MUSTACHE_FILE_NOT_FOUND_500", "해당 경로에 파일이 존재하지 않습니다."),
    MUSTACHE_EXECUTE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MUSTACHE_EXECUTE_FAILED_500", "파일 생성에 실패했습니다."),
    XML_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "XML_PARSING_ERROR_500", "xml 파일 생성에 실패했습니다."),

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
    JENKINS_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "JENKINS_AUTHENTICATION_FAILED_401", "jenkins 인증 실패!"),
    JENKINS_ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "JENKINS_ENDPOINT_NOT_FOUND_404", "올바르지 않은 endpoint입니다."),
    JENKINS_URI_NOT_FOUND(HttpStatus.NOT_FOUND, "JENKINS_URI_NOT_FOUND_404", "올바르지 않은 jenkins uri입니다."),
    JENKINS_CONNECTION_FAILED(HttpStatus.BAD_REQUEST, "JENKINS_CONNECTION_FAILED_400", "jenkins 연결 실패"),
    JENKINS_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_SERVER_ERROR_500", "jenkins 서버 오류"),
    JENKINS_CONNECTION_TIMEOUT_OR_NETWORK_ERROR(HttpStatus.GATEWAY_TIMEOUT, "JENKINS_CONNECTION_TIMEOUT_OR_NETWORK_ERROR_504", "time out"),
    JENKINS_SECRET_ENCRYPTION_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_SECRET_ENCRYPTION_FAIL_500", "암호화 실패 오류"),
    JENKINS_SECRET_DECRYPTION_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_SECRET_DECRYPTION_FAIL_500", "복호화 실패 오류"),

    /**
     * Jenkins/error 도메인에서 사용하는 ErrorCode
     */
    JENKINS_API_CALL_FAILED(HttpStatus.BAD_GATEWAY, "JENKINS_API_CALL_FAILED_502", "젠킨스 API 호출에 실패했습니다."),
    JENKINS_JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "JENKINS_JOB_NOT_FOUND_404", "해당 Job을 찾을 수 없습니다."),
    JENKINS_BUILD_INFO_MISSING(HttpStatus.BAD_REQUEST, "JENKINS_BUILD_INFO_MISSING_400", "빌드 정보가 존재하지 않습니다."),
    JENKINS_INFO_UNAUTHORIZED(HttpStatus.FORBIDDEN, "JENKINS_INFO_UNAUTHORIZED_403", "접근 권한이 없는 Jenkins 설정입니다."),
    JENKINS_NO_JOBS_FOUND(HttpStatus.NOT_FOUND, "JENKINS_NO_JOBS_FOUND_404", "등록된 Jenkins Job이 존재하지 않습니다."),
    JENKINS_ALL_JOBS_FAILED(HttpStatus.BAD_GATEWAY, "JENKINS_ALL_JOBS_FAILED_502", "전체 Job의 빌드 조회에 실패했습니다."),



    /**
     * Jenkins/Job/FreeStyle 도메인에서 사용하는 ErrorCode
     */
    JENKINS_FREESTYLE_NOT_FOUND(HttpStatus.BAD_REQUEST, "JENKINS_FREESTYLE_NOT_FOUND_400", "해당 freestyle job이 존재하지 않습니다."),
    JENKINS_FREESTYLE_HISTORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "JENKINS_FREESTYLE_HISTORY_NOT_FOUND_400", "해당 freestyle history가 존재하지 않습니다."),

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
