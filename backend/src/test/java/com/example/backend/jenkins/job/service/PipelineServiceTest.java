package com.example.backend.jenkins.job.service;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.CustomException;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.PipelineVersion;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.jenkins.job.repository.PipelineVersionRepository;
import com.example.backend.jenkins.notification.repository.JobNotificationRepository;
import com.example.backend.jenkins.notification.service.JobNotificationService;
import com.example.backend.service.HttpClientService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PipelineServiceTest {

    @InjectMocks
    private PipelineService pipelineService;

    @Mock
    private HttpClientService httpClientService;
    @Mock
    private JenkinsInfoService jenkinsInfoService;
    @Mock
    private ConfigService configService;
    @Mock
    private ScriptService scriptService;
    @Mock
    private PipelineRepository pipelineRepository;
    @Mock
    private StageService stageService;
    @Mock
    private PipelineVersionRepository pipelineVersionRepository;
    @Mock
    private JobNotificationService jobNotificationService;
    @Mock
    private JobNotificationRepository jobNotificationRepository;




    //1. createJob
            /*
             * [createJob() 동작 흐름 요약]
             * 1. JenkinsInfo 조회 → jenkinsInfoService.getJenkinsInfo()
             * 2. Job 이름 중복 검사 → ensureUniqueName()
             * 3. Script 조회 → scriptService.getScriptById()
             * 4. (조건) 알림 리스트가 있으면 알림 생성 및 script 수정
             * 5. config XML 생성 → configService.buildConfigContext() + createConfig()
             * 6. Pipeline + Version 저장 → pipelineRepository.save()
             * 7. 알림에 pipelineId 주입 후 저장 (조건부)
             * 8. Jenkins HTTP 호출 → httpClientService.callJenkins()
             */
    @Test
    @DisplayName("[성공] Jenkins Job 생성 성공 - pipeline + version + Jenkins 호출 정상 동작")
    void createJob_success() {

        UUID infoId = UUID.randomUUID();
        UUID scriptId = UUID.randomUUID();

        RequestDto.CreateDto dto = RequestDto.CreateDto.builder()
                .infoId(infoId)
                .scriptId(scriptId)
                .name("job-name")
                .description("some desc")
                .trigger(true)
                .schedule("매일 오전 9시")
                .build();

        JenkinsInfo info = new JenkinsInfo();
        Script script = new Script();
        Pipeline pipeline = Pipeline.builder()
                .id(UUID.randomUUID())
                .jenkinsInfo(info)
                .name(dto.getName())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        //mock 설정
        // 1. JenkinsInfo 조회
        when(jenkinsInfoService.getJenkinsInfo(infoId)).thenReturn(info);
        // 3. Script 조회
        when(scriptService.getScriptById(scriptId)).thenReturn(script);
        // 5. context Map 생성
        when(configService.buildConfigContext(any(), any())).thenReturn(
                Map.of());
        // 5. xml리턴
        when(configService.createConfig(any())).thenReturn("<xml/>");
        // 6. pipeline 저장
        when(pipelineRepository.save(any())).thenReturn(pipeline);
        // 6. pipeline version 저장
        when(pipelineVersionRepository.save(any())).thenReturn(
                PipelineVersion.builder().id(UUID.randomUUID()).build()
        );

        // 실제 서비스 호출
        pipelineService.createJob(dto);

        // 예상되는 동작이 일어났는지 검증
        verify(configService).createConfig(any());
        verify(pipelineRepository, atLeastOnce()).save(any(Pipeline.class));
        verify(pipelineVersionRepository).save(any(PipelineVersion.class));
        verify(stageService).createStages(any(PipelineVersion.class), any(Script.class));
        verify(httpClientService).callJenkins(
                anyString(), anyString(), eq(info), eq(HttpMethod.POST), any(Runnable.class));
    }

    @Test
    @DisplayName("[예외] 중복된 Job 이름이 존재할 경우 CustomException 발생")
    void createJob_duplicateName_shouldThrowCustomException() {
        // given
        UUID infoId = UUID.randomUUID();
        UUID scriptId = UUID.randomUUID();

        RequestDto.CreateDto dto = RequestDto.CreateDto.builder()
                .infoId(infoId)
                .scriptId(scriptId)
                .name("duplicate-job")
                .description("desc")
                .trigger(true)
                .schedule("매일 오전 9시")
                .build();


        JenkinsInfo info = new JenkinsInfo();
        info.setId(infoId);


        Pipeline pipeline = Pipeline.builder()
                .id(UUID.randomUUID())
                .jenkinsInfo(info)
                .name(dto.getName())
                .isDeleted(false)
                .versionList(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();


        when(jenkinsInfoService.getJenkinsInfo(infoId)).thenReturn(info);
        when(pipelineRepository.findByJenkinsInfoIdAndName(infoId, "duplicate-job"))
                .thenReturn(Optional.of(pipeline));

        assertThrows(CustomException.class, () -> {
            pipelineService.createJob(dto);
        });

        verify(pipelineRepository, never()).save(any());
        verify(httpClientService, never()).callJenkins(any(), any(), any(), any(), any());
    }


    // 2. updateJob
    /*
     * [updateJob() 동작 흐름 요약]
     * 1. Pipeline 조회 (Version 포함) → getPipelineById()
     * 2. JenkinsInfo 조회 → pipeline.getJenkinsInfo()
     * 3. 기존 Job 이름과 변경 이름 비교 → isRenamed()
     *
     *  [Case A] 이름이 변경되지 않은 경우:
     *    4A. 최신 Script 로직 조회 → scriptService.getScriptById()
     *    5A. config XML 생성 → configService.buildConfigContext() + createConfig()
     *    6A. Jenkins Job 덮어쓰기(재생성) → httpClientService.callJenkins()
     *
     *  [Case B] 이름이 변경된 경우:
     *    4B. 기존 Job 삭제 요청 → httpClientService.deleteJobOnJenkins()
     *    5B. Pipeline 이름 변경 + 새 버전 생성 → pipeline.setName(), createVersion()
     *    6B. config XML 생성 → configService.buildConfigContext() + createConfig()
     *    7B. Jenkins 새 Job 생성 → httpClientService.callJenkins()
     */

    @Test
    @DisplayName("[성공] updateJob - 기존 이름 그대로인 경우 Jenkins config.xml 업데이트")
    void updateJob_sameName_success() {
        UUID pipelineId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID scriptId = UUID.randomUUID();
        UUID infoId = UUID.randomUUID();
        String jobName = "existing-job";

        JenkinsInfo info = new JenkinsInfo();
        info.setId(infoId);
        info.setUri("http://jenkins.example.com");
        info.setApiToken("encryptedTokenString");

        Script script = new Script();
        script.setScript("pipeline { post { ... } }");

        PipelineVersion version = PipelineVersion.builder()
                .id(versionId)
                .script(script)
                .build();

        Pipeline pipeline = Pipeline.builder()
                .id(pipelineId)
                .name(jobName)
                .jenkinsInfo(info)
                .latestVersionId(versionId)
                .versionList(List.of(version))
                .isDeleted(false)
                .build();

        version.setPipeline(pipeline);
        version.setStageList(new ArrayList<>());

        RequestDto.UpdateDto dto = RequestDto.UpdateDto.builder()
                .pipelineId(pipelineId)
                .name(jobName)
                .description("변경된 설명")
                .trigger(true)
                .schedule("매일 오전 10시")
                .scriptId(scriptId)
                .build();


        when(pipelineRepository.findWithInfoAndScriptById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineVersionRepository.findWithScriptAndStageListById(versionId)).thenReturn(Optional.of(version));
        when(scriptService.getScriptById(scriptId)).thenReturn(script);
        when(pipelineVersionRepository.findById(versionId)).thenReturn(Optional.of(version));
        when(configService.buildConfigContext(any(), any())).thenReturn(Map.of());
        when(configService.createConfig(any())).thenReturn("<xml/>");
        when(pipelineRepository.save(any())).thenReturn(pipeline);


        pipelineService.updateJob(dto);

        verify(httpClientService).callJenkins(
                eq(info.getUri() + "/job/" + jobName + "/config.xml"),
                eq("<xml/>"),
                eq(info),
                eq(HttpMethod.POST),
                any()
        );

        verify(httpClientService, never()).deleteJobOnJenkins(any(), any(), any());
    }

    @Test
    @DisplayName("[성공] updateJob - 이름 변경 시 기존 Job 삭제 및 새로운 Job 생성 호출 확인")
    void updateJob_rename_success() {
        UUID pipelineId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID scriptId = UUID.randomUUID();
        UUID infoId = UUID.randomUUID();

        String oldName = "job-old";
        String newName = "job-new";

        JenkinsInfo info = JenkinsInfo.builder()
                .id(infoId)
                .uri("http://localhost:8080")
                .user(Users.builder().id(UUID.randomUUID()).build())
                .build();

        Script script = Script.builder()
                .id(scriptId)
                .script("pipeline { ... }")
                .build();

        Pipeline pipeline = Pipeline.builder()
                .id(pipelineId)
                .name(oldName)
                .jenkinsInfo(info)
                .latestVersionId(versionId)
                .versionList(new ArrayList<>())
                .isDeleted(false)
                .build();

        PipelineVersion version = PipelineVersion.builder()
                .id(versionId)
                .script(script)
                .pipeline(pipeline)
                .build();
        pipeline.setVersionList(List.of(version));

        RequestDto.UpdateDto dto = RequestDto.UpdateDto.builder()
                .pipelineId(pipelineId)
                .name(newName)
                .description("수정된 job입니다")
                .trigger(true)
                .schedule("매일 오전 9시")
                .scriptId(scriptId)
                .build();


        when(pipelineRepository.findWithInfoAndScriptById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineVersionRepository.findById(versionId)).thenReturn(Optional.of(version));
        when(pipelineVersionRepository.findWithScriptAndStageListById(versionId)).thenReturn(Optional.of(version));
        when(scriptService.getScriptById(scriptId)).thenReturn(script);
        when(configService.buildConfigContext(dto, script)).thenReturn(Map.of());
        when(configService.createConfig(any())).thenReturn("<xml/>");


        pipelineService.updateJob(dto);


        verify(httpClientService).deleteJobOnJenkins(
                eq(info), eq(oldName), any()
        );

        verify(httpClientService).callJenkins(
                eq(info.getUri() + "/createItem?name=" + newName),
                any(), eq(info), eq(HttpMethod.POST), any()
        );
    }


}
