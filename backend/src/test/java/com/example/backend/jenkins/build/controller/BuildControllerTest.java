package com.example.backend.jenkins.build.controller;

import com.example.backend.config.jwt.JwtAuthenticationFilter;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.exception.BaseResponse;
import com.example.backend.jenkins.build.model.JobType;
import com.example.backend.jenkins.build.model.dto.BuildRequestDto;
import com.example.backend.jenkins.build.model.dto.BuildResponseDto;
import com.example.backend.jenkins.build.service.BuildService;
import com.example.backend.jenkins.error.service.ErrorService;
import com.example.backend.jenkins.job.service.PipelineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BuildController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class BuildControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    BuildService buildService;
    @MockitoBean
    PipelineService pipelineService;
    @MockitoBean
    JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    ErrorService errorService;

    @WithMockUser
    @Test
    @DisplayName("빌드 스테이지 목록 조회")
    void job의_스테이지_목록_조회() throws Exception {
        UUID pipelineId = UUID.randomUUID();
        BuildResponseDto.Stage stage = new BuildResponseDto.Stage(List.of("BUILD", "TEST"));

        when(buildService.getJobPipelineStage(any())).thenReturn(stage);

        mockMvc.perform(get("/api/build/stage")
                        .param("pipeLine", pipelineId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stage").isArray());
    }

    @WithMockUser
    @Test
    @DisplayName("특정 스테이지 실행")
    void 특정_스테이지_실행() throws Exception {
        UUID pipelineId = UUID.randomUUID();
        BuildRequestDto.BuildStageRequestDto dto = new BuildRequestDto.BuildStageRequestDto(Map.of("TEST", true), pipelineId);

        mockMvc.perform(post("/api/build/stage/trigger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("특정 Steps 실행"));
    }

    @WithMockUser
    @Test
    @DisplayName("빌드 이력 조회 - LATEST")
    void 빌드_이력_조회_LATEST() throws Exception {
        UUID pipelineId = UUID.randomUUID();
        BuildRequestDto.getBuildHistory dto = new BuildRequestDto.getBuildHistory(JobType.LATEST, pipelineId);

        BuildResponseDto.BuildInfo latest = BuildResponseDto.BuildInfo.builder()
                .jobName("woojin_test1")
                .buildNumber(2)
                .status("FAILURE")
                .durationStr("0.4초")
                .startedAt("2025-07-13 14:05:59")
                .triggeredBy("서찬영")
                .buildUrl("http://122.40.225.54:7979/job/woojin_test1/2/")
                .building(false)
                .build();

        // ✅ 정확한 타입으로 래핑
        BaseResponse<BuildResponseDto.BuildInfo> baseResponse = BaseResponse.success(latest);
        ResponseEntity<BaseResponse<BuildResponseDto.BuildInfo>> responseEntity = ResponseEntity.ok(baseResponse);

        doReturn(responseEntity)
                .when(buildService)
                .getBuildInfo(any(BuildRequestDto.getBuildHistory.class));

        mockMvc.perform(post("/api/build/builds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.body.data.buildNumber").value(2))
                .andExpect(jsonPath("$.data.body.data.jobName").value("woojin_test1"));
    }


    @DisplayName("빌드 이력 조회 - HISTORY")
    @WithMockUser
    @Test
    void 빌드_이력_조회_HISTORY() throws Exception {
        UUID pipelineId = UUID.randomUUID();
        BuildRequestDto.getBuildHistory dto = new BuildRequestDto.getBuildHistory(JobType.HISTORY, pipelineId);

        List<BuildResponseDto.BuildInfo> history = List.of(
                BuildResponseDto.BuildInfo.builder()
                        .jobName("woojin_test1")
                        .buildNumber(1)
                        .status("SUCCESS")
                        .building(false)
                        .durationStr("0.4초")
                        .startedAt("2025-07-13 13:01:55")
                        .triggeredBy("서찬영")
                        .buildUrl("http://122.40.225.54:7979/job/woojin_test1/1/")
                        .build(),

                BuildResponseDto.BuildInfo.builder()
                        .jobName("woojin_test1")
                        .buildNumber(2)
                        .status("FAILURE")
                        .building(false)
                        .durationStr("0.4초")
                        .startedAt("2025-07-13 14:05:59")
                        .triggeredBy("서찬영")
                        .buildUrl("http://122.40.225.54:7979/job/woojin_test1/2/")
                        .build()
        );



        doReturn(ResponseEntity.ok(BaseResponse.success(history)))
                .when(buildService)
                .getBuildInfo(any(BuildRequestDto.getBuildHistory.class));


        mockMvc.perform(post("/api/build/builds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.body.data.length()").value(2))  // 배열 개수 체크
                .andExpect(jsonPath("$.data.body.data[0].buildNumber").value(1))
                .andExpect(jsonPath("$.data.body.data[0].jobName").value("woojin_test1"))
                .andExpect(jsonPath("$.data.body.data[1].buildNumber").value(2))
                .andExpect(jsonPath("$.data.body.data[1].jobName").value("woojin_test1"));


    }




    @WithMockUser
    @Test
    @DisplayName("빌드 로그 조회")
    void 빌드_로그_조회() throws Exception {
        UUID pipelineId = UUID.randomUUID();
        BuildRequestDto.GetLogRequestDto dto = new BuildRequestDto.GetLogRequestDto("42", pipelineId);

        when(buildService.getBuildLog(any()))
                .thenReturn(new BuildResponseDto.BuildLogDto(List.of("Log contents")));

        mockMvc.perform(post("/api/build/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.log[0]").value("Log contents"));
    }

    @WithMockUser
    @Test
    @DisplayName("빌드 실시간 로그 조회")
    void 빌드_실시간_로그_조회() throws Exception {
        UUID pipelineId = UUID.randomUUID();

        when(buildService.getStreamLog(any()))
                .thenReturn(BuildResponseDto.BuildStreamLogDto.getStreamLog("Realtime log"));

        mockMvc.perform(get("/api/build/streamlog")
                        .param("pipeLine", pipelineId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.log[0]").value("Realtime log"));
    }
}
