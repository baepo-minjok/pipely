package com.example.backend.jenkins.error.service;

import com.example.backend.exception.CustomException;
import com.example.backend.jenkins.error.model.dto.ErrorResponseDto;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.repository.JenkinsInfoRepository;
import com.example.backend.jenkins.job.model.pipeline.Pipeline;
import com.example.backend.jenkins.job.repository.FreeStyleHistoryRepository;
import com.example.backend.jenkins.job.repository.PipelineHistoryRepository;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.jenkins.job.service.FreeStyleJobService;
import com.example.backend.jenkins.job.service.PipelineJobService;
import com.example.backend.service.HttpClientService;
import com.example.backend.auth.user.model.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ErrorServiceTest {

    @InjectMocks
    private ErrorService errorService;


    @Mock private PipelineJobService pipelineJobService;
    @Mock private HttpClientService httpClientService;
    @Mock private LlmService llmService;

    private UUID userId;
    private UUID jobId;
    private JenkinsInfo mockInfo;
    private Pipeline mockJob;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        jobId = UUID.randomUUID();
        Users user = Users.builder().id(userId).build();
        mockInfo = JenkinsInfo.builder().uri("http://jenkins.local").user(user).build();
        mockJob = Pipeline.builder().id(jobId).jobName("test-job").jenkinsInfo(mockInfo).build();
    }

    @Test
    @DisplayName("getVerifiedJob - 소유자이면 정상 반환")
    void getVerifiedJob_success() {
        when(pipelineJobService.getPipelineById(jobId)).thenReturn(mockJob);
        Pipeline result = errorService.getVerifiedJobWithPipeline(jobId, userId);
        assertEquals(mockJob, result);
    }

    @Test
    @DisplayName("getVerifiedJob - 소유자 아님이면 예외 발생")
    void getVerifiedJob_unauthorized() {
        Users otherUser = Users.builder().id(UUID.randomUUID()).build();
        JenkinsInfo otherInfo = JenkinsInfo.builder().user(otherUser).build();
        Pipeline job = Pipeline.builder().jenkinsInfo(otherInfo).build();

        when(pipelineJobService.getPipelineById(jobId)).thenReturn(job);

        assertThrows(CustomException.class, () -> errorService.getVerifiedJobWithPipeline(jobId, userId));
    }

    @Test
    @DisplayName("getRecentBuild - 정상 Jenkins 응답이면 DTO 반환")
    void getRecentBuild_success() {
        Map<String, Object> mockResponse = Map.of(
                "number", 10,
                "result", "FAILURE",
                "timestamp", 1000L,
                "duration", 3000L
        );

        when(httpClientService.buildHeaders(mockInfo, MediaType.APPLICATION_JSON)).thenReturn(new HttpHeaders());
        when(httpClientService.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        ErrorResponseDto.FailedBuild result = errorService.getRecentBuild(mockInfo, "test-job");

        assertEquals("FAILURE", result.getResult());
        assertEquals(10, result.getBuildNumber());
    }

    @Test
    @DisplayName("summarizeBuild - 예외 로그 포함 시 LLM 호출")
    void summarizeBuild_failureLog() {
        String buildLog = "Exception in thread main";
        when(httpClientService.buildHeaders(mockInfo, MediaType.TEXT_PLAIN)).thenReturn(new HttpHeaders());
        when(httpClientService.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(buildLog);
        when(llmService.summarizeBuildLog(anyString())).thenReturn("에러는 ~ 때문입니다.");

        ErrorResponseDto.FailedBuildSummary result = errorService.summarizeBuild(mockInfo, "test-job", 11);

        assertEquals("에러는 ~ 때문입니다.", result.getNaturalResponse());
        assertEquals("test-job", result.getJobName());
    }

    @Test
    @DisplayName("summarizeBuild - 예외 없는 로그는 고정 응답")
    void summarizeBuild_successLog() {
        String log = "빌드 성공!";
        when(httpClientService.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(log);
        when(httpClientService.buildHeaders(any(), eq(MediaType.TEXT_PLAIN))).thenReturn(new HttpHeaders());

        ErrorResponseDto.FailedBuildSummary result = errorService.summarizeBuild(mockInfo, "jobA", 2);

        assertTrue(result.getNaturalResponse().contains("정상적으로 완료"));
    }

    @Test
    @DisplayName("extractVersionFromLog - 정상 추출")
    void extractVersionFromLog() {
        String log = """
                + echo "#VERSION: 2"
                #VERSION: 2
                """;

        String version = errorService.extractVersionFromLog(log);

        assertEquals("2", version);
    }

    @Test
    @DisplayName("extractVersionFromLog - 없으면 null 반환")
    void extractVersionFromLog_missing() {
        String log = "성공적인 로그입니다";
        String version = errorService.extractVersionFromLog(log);
        assertNull(version);
    }
}
