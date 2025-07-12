package com.example.backend.jenkins.build.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.config.jwt.JwtAuthenticationFilter;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.jenkins.build.BuildControllerTestConfig;
import com.example.backend.jenkins.build.controller.BuildController;
import com.example.backend.jenkins.build.model.JobType;
import com.example.backend.jenkins.build.model.dto.BuildRequestDto;
import com.example.backend.jenkins.build.model.dto.BuildResponseDto;
import com.example.backend.jenkins.build.service.BuildService;
import com.example.backend.jenkins.error.service.ErrorService;
import com.example.backend.jenkins.job.service.PipelineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BuildController.class
)
@Import(BuildControllerTestConfig.class)  // MockBean 대체로 등록
class BuildControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;


    @Autowired
    BuildService buildService;

    @Autowired
    PipelineService pipelineService;


    @Test
    void job의_스테이지_목록_조회() throws Exception {
        // given
        UUID pipelineId = UUID.randomUUID();
        BuildResponseDto.Stage mockStage = new BuildResponseDto.Stage(Arrays.asList("BUILD", "TEST"));
        given(buildService.getJobPipelineStage(pipelineId)).willReturn(mockStage);

        // when
        ResultActions result = mockMvc.perform(get("/api/build/stage")
                .param("pipeLine", pipelineId.toString()));

        // then
        result.andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.data.pipelineId").value(pipelineId.toString()))
                .andExpect((ResultMatcher) jsonPath("$.data.stages").isArray());

        verify(buildService).getJobPipelineStage(pipelineId);
    }

    @Test
    void 특정_스테이지_실행() throws Exception {
        // given
        UUID pipelineId = UUID.randomUUID();
        BuildRequestDto.BuildStageRequestDto requestDto =
                new BuildRequestDto.BuildStageRequestDto(Map.of("TEST", true), pipelineId);

        // when
        ResultActions result = mockMvc.perform(post("/api/build/stage/trigger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));


        // then
        result.andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.data").value("특정 Steps 실행"));

        verify(buildService).StageJenkinsBuild(any(BuildRequestDto.BuildStageRequestDto.class));
    }

    @Test
    void 빌드_이력_조회_LATEST() throws Exception {
        UUID pipelineId = UUID.randomUUID();

        BuildRequestDto.getBuildHistory requestDto =
                new BuildRequestDto.getBuildHistory(JobType.LATEST, pipelineId);

        BuildResponseDto.BuildInfo latestHistory =
                BuildResponseDto.BuildInfo.builder()
                        .buildNumber(5)
                        .status("SUCCESS")
                        .build();

        // ✅ 핵심: 컨트롤러가 호출하는 서비스 메서드를 목킹
        given(buildService.getLastBuildStatus(pipelineId))
                .willReturn(latestHistory);

        ResultActions result = mockMvc.perform(post("/api/build/builds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        result.andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.data.buildNumber").value(5))
                .andExpect((ResultMatcher) jsonPath("$.data.status").value("SUCCESS"));

        // ✅ 검증: 어떤 서비스 메서드를 호출했는지 확인
        verify(buildService).getLastBuildStatus(pipelineId);
    }

    @Test
    void 빌드_이력_조회_HISTORY() throws Exception {
        // given
        UUID pipelineId = UUID.randomUUID();
        BuildRequestDto.getBuildHistory requestDto =
                new BuildRequestDto.getBuildHistory(JobType.HISTORY, pipelineId);

        List<BuildResponseDto.BuildInfo> historyList = Arrays.asList(
                BuildResponseDto.BuildInfo.builder().buildNumber(3).status("SUCCESS").build(),
                BuildResponseDto.BuildInfo.builder().buildNumber(2).status("FAILURE").build(),
                BuildResponseDto.BuildInfo.builder().buildNumber(1).status("SUCCESS").build()
        );

        // ✅ 핵심: getBuildHistory 메서드 목킹
        given(buildService.getBuildHistory(pipelineId))
                .willReturn(historyList);

        // when
        ResultActions result = mockMvc.perform(post("/api/build/builds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        result.andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.data").isArray())
                .andExpect((ResultMatcher) jsonPath("$.data.length()").value(3))
                .andExpect((ResultMatcher) jsonPath("$.data[0].buildNumber").value(3))
                .andExpect((ResultMatcher) jsonPath("$.data[0].status").value("SUCCESS"))
                .andExpect((ResultMatcher) jsonPath("$.data[1].buildNumber").value(2))
                .andExpect((ResultMatcher) jsonPath("$.data[1].status").value("FAILURE"))
                .andExpect((ResultMatcher) jsonPath("$.data[2].buildNumber").value(1))
                .andExpect((ResultMatcher) jsonPath("$.data[2].status").value("SUCCESS"));

        // ✅ 호출 검증
        verify(buildService).getBuildHistory(pipelineId);
    }

    @Test
    void 빌드_로그_조회() throws Exception {
        // given
        UUID pipelineId = UUID.randomUUID();
        BuildRequestDto.GetLogRequestDto requestDto =
                new BuildRequestDto.GetLogRequestDto("42", pipelineId);


        given(buildService.getBuildLog(any()))
                .willReturn(new BuildResponseDto.BuildLogDto(Collections.singletonList("Log contents")));

        // when
        ResultActions result = mockMvc.perform(post("/api/build/log")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        result.andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.data.log").value("Log contents"));

        verify(buildService).getBuildLog(any());
    }

    @Test
    void 빌드_실시간_로그_조회() throws Exception {
        // given
        UUID pipelineId = UUID.randomUUID();
        BuildResponseDto.BuildStreamLogDto mockStreamLog =
                BuildResponseDto.BuildStreamLogDto.getStreamLog("Realtime log");


        given(buildService.getStreamLog(pipelineId)).willReturn(mockStreamLog);

        // when
        ResultActions result = mockMvc.perform(get("/api/build/streamlog")
                .param("pipeLine", pipelineId.toString()));

        // then
        result.andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.data.log").isArray())
                .andExpect((ResultMatcher) jsonPath("$.data.log[0]").value("Realtime log"));

        verify(buildService).getStreamLog(pipelineId);
    }
}
