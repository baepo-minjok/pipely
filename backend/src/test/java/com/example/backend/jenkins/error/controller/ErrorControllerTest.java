package com.example.backend.jenkins.error.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.config.jwt.JwtAuthenticationFilter;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.jenkins.error.model.dto.ErrorRequestDto;
import com.example.backend.jenkins.error.model.dto.ErrorResponseDto;
import com.example.backend.jenkins.error.service.ErrorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ErrorController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class ErrorControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ErrorService errorService;
    Users testUser;
    UUID jobId = UUID.randomUUID();
    UUID infoId = UUID.randomUUID();
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter; // 필요 시
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setup() {
        testUser = Users.builder().id(UUID.randomUUID()).build();
    }

    @Test
    @DisplayName("최근 빌드 1건 조회")
    void getRecentBuildTest() throws Exception {
        ErrorResponseDto.FailedBuild mockRes = ErrorResponseDto.FailedBuild.of("JobA", 1, "FAILURE", 1000L, 100L);
        when(errorService.getRecentBuildByJob(eq(jobId), any())).thenReturn(mockRes);

        ErrorRequestDto.JobDto reqDto = new ErrorRequestDto.JobDto(jobId, "JobA");

        mockMvc.perform(post("/api/jenkins-error/recent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(testUser, null)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobName").value("JobA"));
    }

    @Test
    @DisplayName("전체 빌드 조회")
    void getBuildsByJobTest() throws Exception {
        when(errorService.getBuildsForJobByUser(eq(jobId), any()))
                .thenReturn(List.of(ErrorResponseDto.FailedBuild.of("JobA", 1, "FAILURE", 1000L, 100L)));

        ErrorRequestDto.JobDto reqDto = new ErrorRequestDto.JobDto(jobId, "JobA");

        mockMvc.perform(post("/api/jenkins-error/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(testUser, null)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].jobName").value("JobA"));
    }

    @Test
    @DisplayName("실패한 빌드만 조회")
    void getFailedBuildsByJobTest() throws Exception {
        when(errorService.getFailedBuildsForJobByUser(eq(jobId), any()))
                .thenReturn(List.of(ErrorResponseDto.FailedBuild.of("JobA", 2, "FAILURE", 2000L, 200L)));

        ErrorRequestDto.JobDto reqDto = new ErrorRequestDto.JobDto(jobId, "JobA");

        mockMvc.perform(post("/api/jenkins-error/history/failed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(testUser, null)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].buildNumber").value(2));
    }

    @Test
    @DisplayName("전체 최근 빌드 조회")
    void getAllRecentBuildsTest() throws Exception {
        when(errorService.getJenkinsInfoByIdAndUser(eq(infoId), any())).thenReturn(null);
        when(errorService.getRecentBuilds(any())).thenReturn(List.of());

        ErrorRequestDto.JenkinsDto reqDto = new ErrorRequestDto.JenkinsDto(infoId, "JobA");

        mockMvc.perform(post("/api/jenkins-error/recent/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(testUser, null)))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("전체 실패 빌드 조회")
    void getFailedBuildsTest() throws Exception {
        when(errorService.getJenkinsInfoByIdAndUser(eq(infoId), any())).thenReturn(null);
        when(errorService.getFailedBuilds(any())).thenReturn(List.of());

        ErrorRequestDto.JenkinsDto reqDto = new ErrorRequestDto.JenkinsDto(infoId, "JobA");

        mockMvc.perform(post("/api/jenkins-error/failed/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(testUser, null)))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GPT 빌드 요약 응답")
    void getBuildSummaryWithSolutionTest() throws Exception {
        when(errorService.summarizeBuildByJob(any(), any()))
                .thenReturn(ErrorResponseDto.FailedBuildSummary.builder()
                        .jobName("JobA")
                        .buildNumber(1)
                        .naturalResponse("에러는 ~ 때문입니다")
                        .build());

        ErrorRequestDto.JobSummaryDto reqDto = ErrorRequestDto.JobSummaryDto.builder().jobId(jobId).buildNumber(1).build();

        mockMvc.perform(post("/api/jenkins-error/summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(testUser, null)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.naturalResponse").value("에러는 ~ 때문입니다"));
    }


    /*@Test
    @DisplayName("Pipeline 리트라이")
    void retryWithRollbackByTest() throws Exception {
        doNothing().when(errorService).retryWithRollback(eq(jobId), any());

        ErrorRequestDto.RetryDto reqDto = new ErrorRequestDto.RetryDto(jobId);

        mockMvc.perform(post("/api/jenkins-error/retry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(testUser, null)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Retry with rollback triggered."));
    }*/
}
