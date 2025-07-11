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
     * Cookie 관련 ErrorCode
     */
    COOKIE_NOT_FOUND(HttpStatus.BAD_REQUEST, "COOKIE_NOT_FOUND_400", "쿠키가 존재하지 않습니다."),

    /**
     * Mustache에서 사용하는 ErrorCode
     */
    MUSTACHE_FILE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "MUSTACHE_FILE_NOT_FOUND_500", "해당 경로에 파일이 존재하지 않습니다."),
    MUSTACHE_EXECUTE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MUSTACHE_EXECUTE_FAILED_500", "파일 생성에 실패했습니다."),
    XML_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "XML_PARSING_ERROR_500", "xml 파일 생성에 실패했습니다."),

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

    /**
     * Auth/Email 도메인에서 사용하는 ErrorCode
     */
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_SEND_FAILED_500", "이메일 발송 실패"),
    EMAIL_VERIFICATION_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "EMAIL_VERIFICATION_TOKEN_INVALID_400", "유효하지않은 인증 코드 입니다."),

    /**
     * Jenkins/Info 도메인에서 사용하는 ErrorCode
     */
    JENKINS_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "JENKINS_INFO_NOT_FOUND_404", "젠킨스 정보가 존재하지 않습니다"),
    JENKINS_AUTHENTICATION_FAILED(HttpStatus.NOT_FOUND, "JENKINS_AUTHENTICATION_FAILED_404", "jenkins 인증 실패!"),
    JENKINS_ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "JENKINS_ENDPOINT_NOT_FOUND_404", "올바르지 않은 endpoint입니다."),
    JENKINS_URI_NOT_FOUND(HttpStatus.NOT_FOUND, "JENKINS_URI_NOT_FOUND_404", "올바르지 않은 jenkins uri입니다."),
    JENKINS_CONNECTION_FAILED(HttpStatus.NOT_FOUND, "JENKINS_CONNECTION_FAILED_404", "jenkins 연결 실패"),
    JENKINS_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_SERVER_ERROR_500", "jenkins 서버 오류"),
    JENKINS_CONNECTION_TIMEOUT_OR_NETWORK_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_CONNECTION_TIMEOUT_OR_NETWORK_ERROR_500", "time out"),
    JENKINS_SECRET_ENCRYPTION_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_SECRET_ENCRYPTION_FAIL_500", "암호화 실패 오류"),
    JENKINS_SECRET_DECRYPTION_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_SECRET_DECRYPTION_FAIL_500", "복호화 실패 오류"),
    JENKINS_XML_UPDATE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "XML_UPDATE_FAIL_500", "CRON 스케줄 XML 수정 중 오류가 발생했습니다."),

    /**
     * Jenkins/error 도메인에서 사용하는 ErrorCode
     */
    JENKINS_API_CALL_FAILED(HttpStatus.BAD_GATEWAY, "JENKINS_API_CALL_FAILED_502", "젠킨스 API 호출에 실패했습니다."),
    JENKINS_JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "JENKINS_JOB_NOT_FOUND_404", "해당 Job을 찾을 수 없습니다."),
    JENKINS_BUILD_INFO_MISSING(HttpStatus.BAD_REQUEST, "JENKINS_BUILD_INFO_MISSING_400", "빌드 정보가 존재하지 않습니다."),
    JENKINS_INFO_UNAUTHORIZED(HttpStatus.FORBIDDEN, "JENKINS_INFO_UNAUTHORIZED_403", "접근 권한이 없는 Jenkins 설정입니다."),
    JENKINS_NO_JOBS_FOUND(HttpStatus.NOT_FOUND, "JENKINS_NO_JOBS_FOUND_404", "등록된 Jenkins Job이 존재하지 않습니다."),
    JENKINS_ALL_JOBS_FAILED(HttpStatus.BAD_GATEWAY, "JENKINS_ALL_JOBS_FAILED_502", "전체 Job의 빌드 조회에 실패했습니다."),
    JENKINS_NO_FAILED_BUILDS(HttpStatus.NOT_FOUND, "JENKINS_NO_FAILED_BUILDS_404", "해당 Job에는 실패한 빌드가 없습니다."),
    JENKINS_JOB_VERSION_NOT_FOUND(HttpStatus.NOT_FOUND, "JENKINS_JOB_VERSION_NOT_FOUND_404", "설정 버전에 해당하는 JobVersion이 존재하지 않습니다."),
    JENKINS_BUILD_NOT_FAILED(HttpStatus.BAD_REQUEST, "JENKINS_BUILD_NOT_FAILED_400", "현재 빌드는 실패 상태가 아닙니다."),
    JENKINS_VERSION_NOT_FOUND_IN_LOG(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_VERSION_NOT_FOUND_IN_LOG_500", "빌드 로그에서 설정 버전을 찾을 수 없습니다."),
    JENKINS_SUCCESS_BUILD_NOT_FOUND(HttpStatus.NOT_FOUND, "JENKINS_SUCCESS_BUILD_NOT_FOUND_404", "성공한 빌드 이력을 찾을 수 없습니다."),
    JENKINS_PIPELINE_NOT_FOUND(HttpStatus.NOT_FOUND, "JENKINS_PIPELINE_NOT_FOUND_404", "해당 파이프라인을 찾을 수 없습니다."),
    JENKINS_PIPELINE_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "JENKINS_PIPELINE_HISTORY_NOT_FOUND_404", "지정한 파이프라인 버전 이력을 찾을 수 없습니다."),


    /**
     * Jenkins/Job 도메인에서 사용하는 ErrorCode
     */
    JENKINS_FREESTYLE_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "JENKINS_FREESTYLE_HISTORY_NOT_FOUND_404", "해당 freestyle history가 존재하지 않습니다."),
    JENKINS_NOT_SUPPORTED_TOOL(HttpStatus.BAD_REQUEST, "JENKINS_NOT_SUPPORTED_TOOL_400", "지원하지 않는 tool이거나 존재하지 않습니다."),
    JENKINS_SCRIPT_NOT_FOUND(HttpStatus.BAD_REQUEST, "JENKINS_SCRIPT_NOT_FOUND_400", "Script 정보가 존재하지않습니다."),
    JENKINS_SCRIPT_NOT_VALID(HttpStatus.BAD_REQUEST, "JENKINS_SCRIPT_NOT_VALID_400", "Script의 문법이 올바르지 않습니다."),
    JENKINS_JOB_EXIST(HttpStatus.BAD_REQUEST, "JENKINS_JOB_EXIST_400", "Jenkins에 이미 동일한 이름의 Job이 존재합니다."),
    /**
     * Jenkins/build 도메인에서 사용하는 ErrorCode
     */
    JENKINS_BUILD_TRIGGER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_BUILD_TRIGGER_FAILED_500", "젠킨스 빌드 트리거에 실패했습니다."),
    JENKINS_BUILD_HISTORY_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_BUILD_HISTORY_PARSE_ERROR_500", "빌드 이력 정보를 파싱하지 못했습니다."),
    JENKINS_LATEST_BUILD_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_LATEST_BUILD_PARSE_ERROR_500", "최신 빌드 정보를 파싱하지 못했습니다."),
    JENKINS_CONSOLE_LOG_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_CONSOLE_LOG_PARSE_ERROR_500", "콘솔 로그 파싱에 실패했습니다."),
    JENKINS_STREAM_LOG_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_STREAM_LOG_FAILED_500", "스트리밍 로그 조회에 실패했습니다."),
    JENKINS_CONFIG_XML_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_CONFIG_XML_FETCH_FAILED_500", "config.xml 조회에 실패했습니다."),
    JENKINS_CONFIG_XML_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_CONFIG_XML_UPDATE_FAILED_500", "config.xml 업데이트에 실패했습니다."),
    JENKINS_XML_CRON_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_XML_CRON_PARSE_ERROR_500", "cron 설정 XML 파싱에 실패했습니다."),
    JENKINS_TRIGGER_SETTING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "JENKINS_TRIGGER_SETTING_FAILED_500", "Jenkins 트리거 셋팅 중 오류가 발생했습니다."),
    JENKINS_JOB_TYPE_FAILED(HttpStatus.BAD_REQUEST, "JENKINS_JOB_TYPE_FAILED_400", "freestyle 또는 pipeline 파라미터가 필요합니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
