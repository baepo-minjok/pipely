package com.example.backend.jenkins.error.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LlmServiceTest {

    @InjectMocks
    private LlmService llmService;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(llmService, "apiKey", "test-api-key");
    }

    @Test
    @DisplayName("summarizeBuildLog - 정상 응답 시 메시지 추출됨")
    void summarizeBuildLog_success() {
        // given
        String mockLog = "Exception in thread main: NullPointerException";

        // mock OpenAI 응답 구조
        Map<String, Object> openAiResponse = Map.of(
                "choices", List.of(
                        Map.of("message", Map.of("role", "assistant", "content", "NullPointer 발생입니다."))
                )
        );

        Map<String, Object> mockChoice = Map.of("message", openAiResponse);
        Map<String, Object> mockResponseBody = Map.of("choices", List.of(mockChoice));

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(mockResponseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(
                eq("https://api.openai.com/v1/chat/completions"),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(mockResponse);

        // when
        String result = llmService.summarizeBuildLog(mockLog);

        // then
        assertTrue(result.contains("content=NullPointer 발생입니다."));
    }
}
