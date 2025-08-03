package com.example.backend.jenkins.error.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LlmServiceTest {

    @InjectMocks
    private LlmService llmService;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        // apiKey 강제 주입
        org.springframework.test.util.ReflectionTestUtils.setField(llmService, "apiKey", "test-api-key");

        // restTemplate Mock 강제 주입 (생성자에서 new 한 것 덮어쓰기)
        org.springframework.test.util.ReflectionTestUtils.setField(llmService, "restTemplate", restTemplate);
    }


    @Test
    @DisplayName("summarizeBuildLog - 정상 응답 시 content 반환")
    void summarizeBuildLog_success() {
        // given
        String mockLog = "Exception in thread main: NullPointerException";

        // OpenAI 응답 Mock
        Map<String, Object> mockMessage = Map.of(
                "role", "assistant",
                "content", "NullPointer 발생입니다."
        );
        Map<String, Object> mockChoice = Map.of("message", mockMessage);
        Map<String, Object> mockResponseBody = Map.of("choices", List.of(mockChoice));
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(mockResponseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(
                eq("https://api.openai.com/v1/chat/completions"),
                any(HttpEntity.class),
                eq(Map.class))
        ).thenReturn(mockResponse);

        // when
        String result = llmService.summarizeBuildLog(mockLog);

        // then
        assertEquals("NullPointer 발생입니다.", result);
    }
}
