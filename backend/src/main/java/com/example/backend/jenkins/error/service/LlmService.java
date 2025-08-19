package com.example.backend.jenkins.error.service;


import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class LlmService {

    private final RestTemplate restTemplate;

    /**
     * RestTemplate 타임아웃 설정
     */
    private ClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10초
        factory.setReadTimeout(60000);    // 60초
        return factory;
    }

    public LlmService(RestTemplate restTemplate) {
        this.restTemplate = new RestTemplate(createRequestFactory());
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
        return """
다음은 Jenkins 빌드 로그입니다. 로그를 분석해 한국어로 간결한 요약을 만들어 주세요.

출력 규칙(아주 중요):
- '순수 텍스트'만 사용하세요. 마크다운/HTML 문법(#, *, **, `, >, --- 등) 절대 사용 금지.
- 제목 라벨만 사용하고, 각 섹션 사이에 빈 줄 1줄을 넣으세요.
- 목록은 하이픈(-) 또는 번호만 사용하세요. 체크박스([]) 금지.
- 추측 금지: 로그에 없는 정보는 '불명확'이라고 표기.

출력 형식(이 형식을 정확히 지켜 출력):
문제 요약:
- …

주요 원인(최대 3개):
- …

해결 방안(실행 순서, 최대 5개):
1. …
2. …

재발 방지 체크리스트:
- …

핵심 로그(최대 3줄):
- …

분량 가이드: 전체 400~600자 내외로 작성.

[분석 대상 로그 시작]
%s
[분석 대상 로그 끝]
""".formatted(log);
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
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

        return message.get("content").toString();

    }
}


