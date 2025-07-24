package com.example.backend.jenkins.job.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.service.ScriptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.example.backend.jenkins.notification.model.JobNotification.Channel.DISCORD;
import static com.example.backend.jenkins.notification.model.JobNotification.Channel.SLACK;
import static com.example.backend.jenkins.notification.model.JobNotification.EventType.BUILD_FAIL;
import static com.example.backend.jenkins.notification.model.JobNotification.EventType.BUILD_SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScriptController.class)
@AutoConfigureMockMvc(addFilters = false)
class ScriptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScriptService scriptService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    public static class UserWrapper {
        private final Users userEntity;

        public UserWrapper(Users userEntity) {
            this.userEntity = userEntity;
        }

        public Users getUserEntity() {
            return userEntity;
        }
    }

    @BeforeEach
    void setup() {
        Users mockUser = Users.builder()
                .id(UUID.fromString("2cbf8830-5d58-442a-87fe-fb77919705f1"))
                .name("테스트유저")
                .email("test@example.com")
                .build();

        UserWrapper wrapper = new UserWrapper(mockUser);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                wrapper, null, List.of()
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("POST /api/jenkins/job/script/generate - 성공 시 200 반환 및 서비스 호출")
    void generateScript_success() throws Exception {
        RequestDto.ScriptBaseDto requestDto = RequestDto.ScriptBaseDto.builder()
                .scriptId(UUID.fromString("2cbf8830-5d58-442a-87fe-fb77919705f1"))
                .infoId(UUID.fromString("fae4b725-01c5-437c-8988-48b3e6c5a34a"))
                .githubUrl("https://github.com/xxx")
                .branch("main")
                .isBuildSelected(true)
                .isTestSelected(true)
                .isK8sDeploy(true)
                .tag("v1.0.0")
                .sshKeyPath("/home/ubuntu/.ssh/id_rsa")
                .sshPort("22")
                .deployTarget("ubuntu@192.168.0.10")
                .k8sPath("./k8s/deployment.yaml")
                .deploymentName("my-app-deployment")
                .namespace("default")
                .appName("my-app")
                .containerName("my-app-container")
                .imageRepo("ghcr.io/taehoon0518/my-app")
                .port("8080")
                .replicas("2")
                .notificationList(List.of(
                        RequestDto.NotificationDto.builder()
                                .name("디스코드 빌드 실패 알림")
                                .webhookUrl("https://discord.com/api/webhooks/xxx")
                                .shouldNotify(true)
                                .eventType(BUILD_FAIL)
                                .channel(DISCORD)
                                .build(),

                        RequestDto.NotificationDto.builder()
                                .name("슬랙 빌드 실패 알림")
                                .webhookUrl("https://hooks.slack.com/services/yyyy")
                                .shouldNotify(false)
                                .eventType(BUILD_FAIL)
                                .channel(SLACK)
                                .build(),

                        RequestDto.NotificationDto.builder()
                                .name("슬랙 빌드 성공 알림")
                                .webhookUrl("https://hooks.slack.com/services/yyyy")
                                .shouldNotify(false)
                                .eventType(BUILD_SUCCESS)
                                .channel(SLACK)
                                .build()
                ))
                .build();

        ResponseDto.LightScriptDto responseDto = ResponseDto.LightScriptDto.builder()
                .scriptId(requestDto.getScriptId())
                .script("pipeline {\n // generated script... \n}")
                .build();

        // ✅ 파라미터 수정: userName 제거
        when(scriptService.generateScript(any(RequestDto.ScriptBaseDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/jenkins/job/script/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scriptId").value(requestDto.getScriptId().toString()))
                .andExpect(jsonPath("$.data.script").exists())
                .andExpect(jsonPath("$.error").doesNotExist());

        // ✅ verify에서도 userName 제거
        verify(scriptService).generateScript(any(RequestDto.ScriptBaseDto.class));
    }
}