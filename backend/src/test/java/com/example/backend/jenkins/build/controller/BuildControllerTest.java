package com.example.backend.jenkins.build.controller;

import com.example.backend.jenkins.build.controller.BuildController;
import com.example.backend.jenkins.build.model.JobType;
import com.example.backend.jenkins.build.model.dto.BuildRequestDto;
import com.example.backend.jenkins.build.model.dto.BuildResponseDto;
import com.example.backend.jenkins.build.service.BuildService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = BuildController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
class BuildControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BuildService buildService;

    ObjectMapper objectMapper = new ObjectMapper();

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

        Mockito.verify(buildService).getJobPipelineStage(pipelineId);
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

        Mockito.verify(buildService).StageJenkinsBuild(any(BuildRequestDto.BuildStageRequestDto.class));
    }

    @Test
    void 빌드_이력_조회_LATEST() throws Exception {
        // given
        UUID pipelineId = UUID.randomUUID();
        BuildRequestDto.getBuildHistory requestDto =
                new BuildRequestDto.getBuildHistory(JobType.LATEST, pipelineId);

        BuildResponseDto.BuildInfo latestHistory =
                BuildResponseDto.BuildInfo.builder()
                        .buildNumber(5)
                        .status("SUCCESS")
                        .build();

        BDDMockito.given(buildService.getBuildInfo(requestDto))
                .willReturn(ResponseEntity.ok(List.of(latestHistory)));
        // when
        ResultActions result = mockMvc.perform(post("/api/build/builds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        result.andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.data[0].buildNumber").value(5))
                .andExpect((ResultMatcher) jsonPath("$.data[0].status").value("SUCCESS"));

        Mockito.verify(buildService).getBuildInfo(requestDto);
    }

    @Test
    void 빌드_이력_조회_HISTORY() throws Exception {
        // given
        UUID pipelineId = UUID.randomUUID();
        BuildRequestDto.getBuildHistory requestDto =
                new BuildRequestDto.getBuildHistory(JobType.HISTORY, pipelineId);

        List<BuildResponseDto.BuildInfo> historyList = Arrays.asList(
                BuildResponseDto.BuildInfo.builder().buildNumber(4).status("FAILURE").build(),
                BuildResponseDto.BuildInfo.builder().buildNumber(3).status("SUCCESS").build(),
                BuildResponseDto.BuildInfo.builder().buildNumber(2).status("FAILURE").build()
        );


        BDDMockito.given(buildService.getBuildInfo(requestDto))
                .willReturn(ResponseEntity.ok(List.of(historyList)));

        // when
        ResultActions result = mockMvc.perform(post("/api/build/builds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        result.andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.data").isArray())
                .andExpect((ResultMatcher) jsonPath("$.data.length()").value(3))
                .andExpect((ResultMatcher) jsonPath("$.data[0].buildNumber").value(4))
                .andExpect((ResultMatcher) jsonPath("$.data[0].status").value("FAILURE"))
                .andExpect((ResultMatcher) jsonPath("$.data[1].buildNumber").value(3))
                .andExpect((ResultMatcher) jsonPath("$.data[1].status").value("SUCCESS"))
                .andExpect((ResultMatcher) jsonPath("$.data[2].buildNumber").value(2))
                .andExpect((ResultMatcher) jsonPath("$.data[2].status").value("FAILURE"));

        Mockito.verify(buildService).getBuildInfo(requestDto);
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

        Mockito.verify(buildService).getBuildLog(any());
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

        Mockito.verify(buildService).getStreamLog(pipelineId);
    }
}
