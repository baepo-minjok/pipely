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
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER_400", "파라미터가 존재하지 않습니다."),
    IOEXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "IOEXCEPTION_500", "파일 처리 오류입니다."),
    GIT_CLONE_FAILED(HttpStatus.BAD_REQUEST, "GIT_CLONE_FAILED_400", "Git clone에 실패했습니다."),
    METHOD_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "METHOD_UNAUTHORIZED_401", "해당 경로에 대한 권한이 없습니다."),

    /**
     * HttpClientService에서 사용되는 ErrorCode
     */
    INVALID_ENDPOINT(HttpStatus.INTERNAL_SERVER_ERROR, "INVALID_ENDPOINT_500", "존재하지 않는 경로로 보낸 요청입니다."),
    HTTP_REQUEST_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "HTTP_REQUEST_EXCEPTION_500", "Jenkins 연결이 실패했습니다."),
    JENKINS_SERVER_PROBLEM(HttpStatus.SERVICE_UNAVAILABLE, "JENKINS_SERVER_PROBLEM_503", "젠킨스 서버의 문제로 연결이 실패했습니다."),
    URL_INCORRECT(HttpStatus.BAD_REQUEST, "URL_INCORRECT_400", "올바르지 않은 URL로 보낸 요청입니다."),
    DUPLICATED_JOB_NAME(HttpStatus.BAD_REQUEST, "DUPLICATED_JOB_NAME_400", "Jenkins 서버에 동일한 이름의 Job이 있습니다."),
    AUTHENTICATION_FAILED(HttpStatus.BAD_REQUEST, "AUTHENTICATION_FAILED_400", "올바르지 않은 인증 정보입니다."),
    JENKINS_CONNECTION_FAILED(HttpStatus.NOT_FOUND, "JENKINS_CONNECTION_FAILED_404", "올바르지 않은 요청으로 인한 jenkins 연결 실패"),

    /**
     * Cookie 관련 ErrorCode
     */
    COOKIE_NOT_FOUND(HttpStatus.BAD_REQUEST, "COOKIE_NOT_FOUND_400", "쿠키가 존재하지 않습니다."),

    /**
     * Mustache에서 사용하는 ErrorCode
     */
    MUSTACHE_FILE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "MUSTACHE_FILE_NOT_FOUND_500", "해당 경로에 파일이 존재하지 않습니다."),
    MUSTACHE_EXECUTE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MUSTACHE_EXECUTE_FAILED_500", "파일 생성에 실패했습니다."),

    /**
     * Auth/User 도메인에서 사용하는 ErrorCode
     */
    USER_LOGIN_FAILED(HttpStatus.BAD_REQUEST, "USER_LOGIN_FAILED_400", "로그인 실패"),
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
    USER_OAUTH2_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "USER_OAUTH2_TOKEN_INVALID_400", "유효하지않은 토큰입니다."),
    /**
     * Auth/Email 도메인에서 사용하는 ErrorCode
     */
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_SEND_FAILED_500", "이메일 발송 실패"),
    EMAIL_VERIFICATION_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "EMAIL_VERIFICATION_TOKEN_INVALID_400", "유효하지않은 인증 코드 입니다."),

    /**
     * Jenkins/Info 도메인에서 사용하는 ErrorCode
     */
    JENKINS_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "JENKINS_INFO_NOT_FOUND_404", "젠킨스 정보가 존재하지 않습니다"),
    JENKINS_SECRET_ENCRYPTION_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_SECRET_ENCRYPTION_FAIL_500", "암호화 실패 오류"),
    JENKINS_SECRET_DECRYPTION_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_SECRET_DECRYPTION_FAIL_500", "복호화 실패 오류"),
    JENKINS_XML_UPDATE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "XML_UPDATE_FAIL_500", "CRON 스케줄 XML 수정 중 오류가 발생했습니다."),

    /**
     * Jenkins/error 도메인에서 사용하는 ErrorCode
     */
    JENKINS_JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "JENKINS_JOB_NOT_FOUND_404", "해당 Job을 찾을 수 없습니다."),
    JENKINS_BUILD_INFO_MISSING(HttpStatus.BAD_REQUEST, "JENKINS_BUILD_INFO_MISSING_400", "빌드 정보가 존재하지 않습니다."),
    JENKINS_JOB_VERSION_NOT_FOUND(HttpStatus.NOT_FOUND, "JENKINS_JOB_VERSION_NOT_FOUND_404", "JobVersion이 존재하지 않습니다."),
    JENKINS_NO_SUCCESSFUL_BUILD(HttpStatus.NOT_FOUND, "JENKINS_NO_SUCCESSFUL_BUILD_404", "성공한 빌드가 존재하지 않습니다."),


    /**
     * Jenkins/Job 도메인에서 사용하는 ErrorCode
     */
    JENKINS_SCRIPT_NOT_FOUND(HttpStatus.NOT_FOUND, "JENKINS_SCRIPT_NOT_FOUND_404", "Script 정보가 존재하지 않습니다."),
    JENKINS_JOB_EXIST(HttpStatus.BAD_REQUEST, "JENKINS_JOB_EXIST_400", "Jenkins에 이미 동일한 이름의 Job이 존재합니다."),
    CANNOT_DELETE_LATEST_VERSION(HttpStatus.BAD_REQUEST, "CANNOT_DELETE_LATEST_VERSION_400", "최신버전은 삭제 할 수 없습니다."),
    JENKINS_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_API_ERROR_500", "Jenkins API 응답 처리 중 오류가 발생했습니다."),

    /**
     * Jenkins/build 도메인에서 사용하는 ErrorCode
     */
    JENKINS_BUILD_HISTORY_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_BUILD_HISTORY_PARSE_ERROR_500", "빌드 이력 정보를 파싱하지 못했습니다."),
    JENKINS_LATEST_BUILD_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_LATEST_BUILD_PARSE_ERROR_500", "최신 빌드 정보를 파싱하지 못했습니다."),
    JENKINS_CONSOLE_LOG_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_CONSOLE_LOG_PARSE_ERROR_500", "콘솔 로그 파싱에 실패했습니다."),

    /**
     * Jenkins/Job/Notification 도메인에서 사용하는 ErrorCode
     */
    JENKINS_NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "JENKINS_NOTIFICATION_NOT_FOUND_404", "해당 notification이 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
