package com.example.backend.jenkins.error.service;


import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class LlmService {

    private final RestTemplate restTemplate;

    public LlmService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${openai.api-key}")
    private String apiKey;

    /**
     * Jenkins 빌드 로그를 요약하기 위한 메인 메서드.
     * 로그 문자열을 받아서 LLM에게 전달하고 응답을 반환.
     */
    public String summarizeBuildLog(String log) {
        String prompt = buildPrompt(log);
        return callOpenAi(prompt);
    }

    /**
     * 전달받은 Jenkins 로그를 기반으로 LLM에 보낼 프롬프트 텍스트 생성
     */
    private String buildPrompt(String log) {
        return "다음은 Jenkins 빌드 로그입니다. 실패 원인을 요약하고 해결책을 제시해주세요:\n\n" + log;
    }

    /**
     * OpenAI Chat Completions API를 호출하여 요약 응답을 받는 메서드
     */
    private String callOpenAi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey); // API 키

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini", // 사용할 모델
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                request,
                Map.class
        );

        // 응답에서 실제 메시지 추출
        return ((Map)((List)response.getBody().get("choices")).get(0)).get("message").toString();
    }
}


