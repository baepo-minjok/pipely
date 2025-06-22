package com.example.backend;

import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StandaloneTestControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // 직접 생성: 만약 TestController에 서비스 주입이 필요하면, 목 객체를 생성해 생성자 주입
        TestController controller = new TestController(/* 필요한 서비스 혹은 목 */);
        GlobalExceptionHandler advice = new GlobalExceptionHandler();
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(advice)
                .build();
    }

    @Test
    @DisplayName("StandaloneSetup: CustomException 예외 처리 검증")
    void testCustomExceptionStandalone() throws Exception {
        mockMvc.perform(get("/api/test/custom-error"))
                .andExpect(status().isBadRequest()) // ErrorCode.VALIDATION_FAILED 등 매핑된 상태
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.status").value(ErrorCode.VALIDATION_FAILED.getHttpStatus().value()))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.VALIDATION_FAILED.getCode()))
                .andExpect(jsonPath("$.error.message").value(ErrorCode.VALIDATION_FAILED.getMessage()))
                .andExpect(jsonPath("$.error.path").value("/api/test/custom-error"))
                .andExpect(jsonPath("$.error.timestamp").exists())
                .andDo(print());
    }
}
