package com.example.backend;


import com.example.backend.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    @DisplayName("CustomException 발생 시 전역 핸들러 동작 확인")
    void testCustomExceptionHandling() throws Exception {
        // 예시: "/api/test/custom-error" 엔드포인트가 throw new CustomException(...)을 발생시킨다고 가정
        mockMvc.perform(get("/api/test/custom-error"))
                .andExpect(status().isBadRequest()) // ErrorCode.VALIDATION_FAILED 등 매핑된 상태
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.status").value(ErrorCode.VALIDATION_FAILED.getHttpStatus().value()))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.VALIDATION_FAILED.getCode()))
                .andExpect(jsonPath("$.error.message").value(ErrorCode.VALIDATION_FAILED.getMessage()))
                .andExpect(jsonPath("$.error.path").value("/api/test/custom-error"))
                .andExpect(jsonPath("$.error.timestamp").exists());
    }
}
