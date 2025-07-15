package com.example.backend.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CancellationException;

@Slf4j
@Service
@RequiredArgsConstructor
public class HttpClientService {
    private final RestTemplate restTemplate;

    /**
     * HTTP 요청을 보내고, 응답 본문만 반환합니다.
     *
     * @param url           요청 URL
     * @param method        HTTP 메서드 (GET, POST 등)
     * @param requestEntity 헤더·바디가 담긴 HttpEntity
     * @param responseType  응답 바디를 매핑할 클래스
     * @param <T>           응답 타입
     * @return responseType으로 매핑된 본문
     */
    public <T> T exchange(
            String url,
            HttpMethod method,
            HttpEntity<?> requestEntity,
            Class<T> responseType
    ) {
        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    method,
                    requestEntity,
                    responseType
            );
            return response.getBody();
        } catch (IllegalArgumentException e) {
            // url 틀렸을때
            log.error("Invalid url");
            throw new CustomException(ErrorCode.URL_INCORRECT);
        } catch (HttpClientErrorException e) {
            // 4xx 오류
            log.error(e.getStatusCode().toString());
            log.error(e.getResponseBodyAsString());
            log.error("HttpClientErrorException: {}", e.getMessage());

            int status = e.getStatusCode().value();
            switch (status) {
                case 400:
                    throw new CustomException(ErrorCode.DUPLICATED_JOB_NAME);
                case 401:
                    throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
                case 404:
                    throw new CustomException(ErrorCode.INVALID_ENDPOINT);
            }
            throw new CustomException(ErrorCode.JENKINS_CONNECTION_FAILED);
        } catch (HttpServerErrorException e) {
            // 5xx 오류
            log.error(e.getStatusCode().toString());
            log.error("HttpServerErrorException: {}", e.getMessage());
            throw new CustomException(ErrorCode.JENKINS_SERVER_PROBLEM);
        } catch (CancellationException e) {
            // 잘못된 주소로 요청이 취소
            log.error("Http request cancelled: {}", e.getMessage());
            throw new CustomException(ErrorCode.URL_INCORRECT);
        } catch (RestClientException e) {
            // 그외 기타 예외
            log.error("Unhandled request Exception: {}", e.getMessage());
            throw new CustomException(ErrorCode.HTTP_REQUEST_EXCEPTION);
        }
    }

    public HttpHeaders buildHeaders(JenkinsInfo info, MediaType mediaType) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(info.getJenkinsId(), info.getApiToken());
        headers.setContentType(mediaType);

        return headers;
    }
}

