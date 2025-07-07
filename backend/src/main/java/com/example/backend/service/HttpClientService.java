package com.example.backend.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CancellationException;

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
            HttpStatusCode httpStatusCode = response.getStatusCode();

            if (httpStatusCode == HttpStatus.NOT_FOUND) {
                throw new CustomException(ErrorCode.JENKINS_ENDPOINT_NOT_FOUND);
            } else if (httpStatusCode == HttpStatus.UNAUTHORIZED) {
                throw new CustomException(ErrorCode.JENKINS_AUTHENTICATION_FAILED);
            } else if (httpStatusCode == HttpStatus.GATEWAY_TIMEOUT || httpStatusCode == HttpStatus.REQUEST_TIMEOUT) {
                throw new CustomException(ErrorCode.JENKINS_CONNECTION_TIMEOUT_OR_NETWORK_ERROR);
            } else if (httpStatusCode.is5xxServerError()) {
                throw new CustomException(ErrorCode.JENKINS_SERVER_ERROR);
            } else if (httpStatusCode.is4xxClientError()) {
                throw new CustomException(ErrorCode.JENKINS_CONNECTION_FAILED);
            } else {
                return response.getBody();
            }

        } catch (CancellationException ex) {
            throw new CustomException(ErrorCode.JENKINS_URI_NOT_FOUND);
        }
    }

    public HttpHeaders buildHeaders(JenkinsInfo info, MediaType mediaType) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(info.getJenkinsId(), info.getApiToken());
        headers.setContentType(mediaType);

        return headers;
    }
}

