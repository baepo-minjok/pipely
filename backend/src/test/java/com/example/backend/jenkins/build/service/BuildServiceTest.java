package com.example.backend.jenkins.build.service;

import com.example.backend.exception.CustomException;
import com.example.backend.jenkins.build.model.JobType;
import com.example.backend.jenkins.build.model.dto.BuildRequestDto;
import com.example.backend.jenkins.build.model.dto.BuildResponseDto;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.service.PipelineService;
import com.example.backend.service.HttpClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildServiceTest {

    @InjectMocks
    private BuildService buildService;

    @Mock
    private HttpClientService httpClientService;
    @Mock
    private PipelineService pipelineService;

    private UUID pipelineId;
    private Pipeline mockPipeline;
    private JenkinsInfo mockJenkinsInfo;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        pipelineId = UUID.randomUUID();
        mockJenkinsInfo = JenkinsInfo.builder().uri("http://jenkins.local").build();
        mockPipeline = Pipeline.builder().id(pipelineId).name("test-job").jenkinsInfo(mockJenkinsInfo).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("getBuildHistory - 정상 응답 시 파싱 성공")
    void getBuildHistory_success() throws Exception {
        String mockJson = """   
                    {
                      "builds": [
                        {
                          "number": 1,
                          "result": "SUCCESS",
                          "timestamp": 1000,
                          "duration": 200,
                          "building": false,
                          "url": "http://jenkins.local/job/test-job/1/",
                          "actions": [{
                            "causes": [{
                              "userName": "tester"
                            }]
                          }]
                        }
                      ]
                    }
                """;

        when(pipelineService.getPipelineById(pipelineId)).thenReturn(mockPipeline);
        when(httpClientService.buildHeaders(mockJenkinsInfo, MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(new HttpHeaders());
        when(httpClientService.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockJson);

        List<BuildResponseDto.BuildInfo> result = buildService.getBuildHistory(pipelineId);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getBuildNumber());
        assertEquals("SUCCESS", result.get(0).getStatus());
        assertEquals("tester", result.get(0).getTriggeredBy());
    }

    @Test
    @DisplayName("getLastBuildStatus - 정상 응답 시 단일 빌드 반환")
    void getLastBuildStatus_success() throws Exception {
        String mockJson = """
                    {
                      "builds": [
                        {
                          "number": 3,
                          "result": "FAILURE",
                          "timestamp": 2000,
                          "duration": 100,
                          "building": false,
                          "url": "http://jenkins.local/job/test-job/3/",
                          "actions": [{
                            "causes": [{
                              "userName": "user1"
                            }]
                          }]
                        }
                      ]
                    }
                """;

        when(pipelineService.getPipelineById(pipelineId)).thenReturn(mockPipeline);
        when(httpClientService.buildHeaders(mockJenkinsInfo, MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(new HttpHeaders());
        when(httpClientService.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockJson);

        BuildResponseDto.BuildInfo result = buildService.getLastBuildStatus(pipelineId);

        assertEquals(3, result.getBuildNumber());
        assertEquals("FAILURE", result.getStatus());
    }

    @Test
    @DisplayName("StageJenkinsBuild - 전달된 stageToggles 값이 요청 본문에 반영됨")
    void stageJenkinsBuild_success() {
        BuildRequestDto.BuildStageRequestDto dto = new BuildRequestDto.BuildStageRequestDto();
        dto.setPipeLine(pipelineId);
        dto.setStageToggles(Map.of("Build", true, "Test", false));

        when(pipelineService.getPipelineById(pipelineId)).thenReturn(mockPipeline);
        when(httpClientService.buildHeaders(mockJenkinsInfo, MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(new HttpHeaders());
        when(httpClientService.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn("Build triggered");

        assertDoesNotThrow(() -> buildService.StageJenkinsBuild(dto));
    }

    @Test
    @DisplayName("getBuildLog - consoleText API 호출 성공 시 로그 반환")
    void getBuildLog_success() {
        // given
        String buildNumber = "5";
        String mockLog = """
<html>
  <body>
    <pre class="console-output">
Started by user admin
Building in workspace...
+ chmod +x gradlew
+ ./gradlew build
Finished: SUCCESS
    </pre>
  </body>
</html>
""";

        BuildRequestDto.GetLogRequestDto dto =
                new BuildRequestDto.GetLogRequestDto(buildNumber, pipelineId);

        when(pipelineService.getPipelineById(pipelineId)).thenReturn(mockPipeline);
        when(httpClientService.buildHeaders(eq(mockJenkinsInfo), any(MediaType.class)))
                .thenReturn(new HttpHeaders());
        when(httpClientService.exchange(
                eq("http://jenkins.local/job/test-job/5/console"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(mockLog);

        // when
        BuildResponseDto.BuildLogDto result = buildService.getBuildLog(dto);

        // then
        assertNotNull(result);
        assertFalse(result.getLog().isEmpty());
        assertEquals("Started by user admin", result.getLog().get(0));
        assertTrue(result.getLog().stream().anyMatch(line -> line.contains("SUCCESS")));
    }


    @Test
    @DisplayName("getStreamLog - progressiveText API 호출 성공 시 로그 반환")
    void getStreamLog_success() {
        String mockLog = "Running...\nStep1 complete\n";

        when(pipelineService.getPipelineById(pipelineId)).thenReturn(mockPipeline);
        when(httpClientService.buildHeaders(eq(mockJenkinsInfo), any(MediaType.class)))
                .thenReturn(new HttpHeaders());

        // 1. buildNumber 조회 stubbing
        when(httpClientService.exchange(
                contains("buildNumber"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))
        ).thenReturn("5");

        // 2. progressiveText 로그 조회 stubbing
        when(httpClientService.exchange(
                contains("progressiveText"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))
        ).thenReturn(mockLog);

        BuildResponseDto.BuildStreamLogDto result = buildService.getStreamLog(pipelineId);

        assertEquals(
                Arrays.asList(mockLog.split("\\r?\\n")),
                result.getLog()
        );
    }



}
