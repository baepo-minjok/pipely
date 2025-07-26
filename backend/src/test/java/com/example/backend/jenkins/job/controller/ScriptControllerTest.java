package com.example.backend.jenkins.job.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.service.ScriptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.example.backend.jenkins.notification.model.JobNotification.Channel.DISCORD;
import static com.example.backend.jenkins.notification.model.JobNotification.EventType.BUILD_FAIL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScriptController.class)
@AutoConfigureMockMvc(addFilters = false)
class ScriptControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private ScriptService scriptService;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;

    private UUID userId;
    private UUID scriptId;
    private UUID infoId;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        scriptId = UUID.randomUUID();
        infoId = UUID.randomUUID();

        Users mockUser = Users.builder()
                .id(userId)
                .name("테스트유저")
                .email("test@example.com")
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        new Object() {
                            public Users getUserEntity() {
                                return mockUser;
                            }
                        },
                        null,
                        List.of()
                );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /* -------------------- POST /generate -------------------- */

    @Test
    @DisplayName("POST /generate - Script 생성 성공 시 200 반환")
    void generateScript_success() throws Exception {
        RequestDto.ScriptBaseDto requestDto = RequestDto.ScriptBaseDto.builder()
                .scriptId(scriptId)
                .infoId(infoId)
                .githubUrl("https://github.com/example")
                .branch("main")
                .isBuildSelected(true)
                .isTestSelected(true)
                .isK8sDeploy(false)
                .tag("v1.0.0")
                .sshKeyPath("/key")
                .sshPort("22")
                .deployTarget("ubuntu@1.2.3.4")
                .notificationList(List.of(RequestDto.NotificationDto.builder()
                        .channel(DISCORD)
                        .eventType(BUILD_FAIL)
                        .webhookUrl("https://discord.com/xxx")
                        .shouldNotify(true)
                        .name("빌드 실패 시 디스코드 알람").build()))
                .build();

        ResponseDto.LightScriptDto responseDto = ResponseDto.LightScriptDto.builder()
                .scriptId(scriptId)
                .githubUrl("https://github.com/example")
                .branch("main")
                .isBuildSelected(true)
                .isTestSelected(true)
                .isK8sDeploy(false)
                .tag("v1.0.0")
                .sshKeyPath("/key")
                .sshPort("22")
                .deployTarget("ubuntu@1.2.3.4")
                .script("pipeline { ... }")
                .build();

        when(scriptService.generateScript(any())).thenReturn(responseDto);

        mockMvc.perform(post("/api/jenkins/job/script/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scriptId").value(scriptId.toString()))
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(scriptService, times(1)).generateScript(any());
    }

    @Test
    @DisplayName("POST /generate - 필수 필드 누락 시 400 반환")
    void generateScript_missingFields_returnsBadRequest() throws Exception {
        RequestDto.ScriptBaseDto dto = new RequestDto.ScriptBaseDto();

        mockMvc.perform(post("/api/jenkins/job/script/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED_400"));
    }

    @Test
    @DisplayName("POST /generate - 존재하지 않는 ScriptId 수정 시 404 반환")
    void generateScript_notFoundScriptId_returnsNotFound() throws Exception {
        RequestDto.ScriptBaseDto dto = RequestDto.ScriptBaseDto.builder()
                .scriptId(UUID.randomUUID())
                .infoId(infoId)
                .githubUrl("https://github.com/example")
                .branch("main")
                .isBuildSelected(true)
                .isTestSelected(true)
                .build();

        doThrow(new CustomException(ErrorCode.JENKINS_SCRIPT_NOT_FOUND))
                .when(scriptService).generateScript(any());

        mockMvc.perform(post("/api/jenkins/job/script/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("JENKINS_SCRIPT_NOT_FOUND_404"));
    }

    /* -------------------- POST /validate -------------------- */

    @Test
    @DisplayName("POST /validate - Script 유효성 검증 성공 시 200 반환")
    void validateScript_success() throws Exception {
        RequestDto.ScriptValidateDto dto = new RequestDto.ScriptValidateDto();
        dto.setInfoId(infoId);
        dto.setScript("pipeline { ... }");

        mockMvc.perform(post("/api/jenkins/job/script/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Script validation success"));

        verify(scriptService, times(1)).validateScript(any());
    }

    @Test
    @DisplayName("POST /validate - script 필드 누락 시 400 반환")
    void validateScript_missingScript_returnsBadRequest() throws Exception {
        RequestDto.ScriptValidateDto dto = new RequestDto.ScriptValidateDto();
        dto.setInfoId(infoId);

        mockMvc.perform(post("/api/jenkins/job/script/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED_400"));
    }

    @Test
    @DisplayName("POST /validate - 존재하지 않는 JenkinsInfo 참조 시 404 반환")
    void validateScript_jenkinsInfoNotFound_returnsNotFound() throws Exception {
        RequestDto.ScriptValidateDto dto = new RequestDto.ScriptValidateDto();
        dto.setInfoId(infoId);
        dto.setScript("pipeline {}");

        doThrow(new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND))
                .when(scriptService).validateScript(any());

        mockMvc.perform(post("/api/jenkins/job/script/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("JENKINS_INFO_NOT_FOUND_404"));
    }

    /* -------------------- DELETE / -------------------- */

    @Test
    @DisplayName("DELETE / - Script 삭제 성공 시 200 반환")
    void deleteScript_success() throws Exception {
        mockMvc.perform(delete("/api/jenkins/job/script")
                        .param("scriptId", scriptId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Script deleted success"))
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(scriptService, times(1)).deleteScript(scriptId);
    }

    @Test
    @DisplayName("DELETE / - 잘못된 UUID 형식 파라미터 시 400 반환")
    void deleteScript_invalidUUID_returnsBadRequest() throws Exception {
        mockMvc.perform(delete("/api/jenkins/job/script")
                        .param("scriptId", "invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED_400"));
    }

    @Test
    @DisplayName("DELETE / - 존재하지 않는 ScriptId 삭제 시 404 반환")
    void deleteScript_notFound_returnsNotFound() throws Exception {
        doThrow(new CustomException(ErrorCode.JENKINS_SCRIPT_NOT_FOUND))
                .when(scriptService).deleteScript(scriptId);

        mockMvc.perform(delete("/api/jenkins/job/script")
                        .param("scriptId", scriptId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("JENKINS_SCRIPT_NOT_FOUND_404"));
    }
}
