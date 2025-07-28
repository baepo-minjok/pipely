package com.example.backend.jenkins.job.service;


import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.PipelineVersion;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.jenkins.job.repository.PipelineVersionRepository;
import com.example.backend.service.HttpClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class CompensationServiceTest {

    @InjectMocks
    private CompensationService compensationService;

    @Mock
    private PipelineRepository pipelineRepository;

    @Mock
    private StageService stageService;

    @Mock
    private HttpClientService httpClientService;

    @Mock
    private PipelineVersionRepository pipelineVersionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * [CompensationService 요약]
     * CompensationService는 외부(Jenkins) 요청이 실패했을 때를 대비하여
     * 내부 DB 상태를 수동으로 보상(rollback) 처리하는 역할을 한다.
     *
     * - DB 트랜잭션 내에서 외부 HTTP 요청은 롤백 대상이 아니기 때문에
     *   요청 실패 시 직접 DB 상태를 복원해줄 수단이 필요함
     * - CompensationService는 이런 복원 처리(삭제, 재생성, 복사 등)를 담당한다
     */
    @Test
    @DisplayName("deletePipeline - 파이프라인이 존재할 경우 삭제한다")
    void deletePipeline_shouldDeleteIfExists() {
        UUID id = UUID.randomUUID();
        Pipeline pipeline = new Pipeline();
        when(pipelineRepository.findById(id)).thenReturn(Optional.of(pipeline));

        compensationService.deletePipeline(id);

        verify(pipelineRepository).delete(pipeline);
    }

    @Test
    @DisplayName("softDeletePipeline - hard Delete 방지용으로 삭제 시간을 업데이트하고 저장한다")
    void softDeletePipeline_shouldUpdateAndSave() {
        Pipeline pipeline = new Pipeline();
        LocalDateTime now = LocalDateTime.now();

        compensationService.softDeletePipeline(pipeline, now, true);

        assertThat(pipeline.getDeletedAt()).isEqualTo(now);
        assertThat(pipeline.getIsDeleted()).isTrue();
        verify(pipelineRepository).save(pipeline);
        verify(pipelineRepository).flush();
    }

    @Test
    @DisplayName("rollback - 전달받은 버전에 대해 Stage 연결을 복원하고 저장한다")
    void rollback_shouldUpdateStagesAndSave() {
        PipelineVersion version = new PipelineVersion();
        Script script = new Script();
        version.setScript(script);

        compensationService.rollback(version);

        verify(stageService).updateStages(version, script);
        verify(pipelineVersionRepository).save(version);
    }

    @Test
    @DisplayName("reCreateJob - DB rollback과 Jenkins 서버에 config를 이전 버전으로 돌린다")
    void reCreateJob_shouldCallRollbackAndHttpClient() {
        PipelineVersion version = new PipelineVersion();
        Script script = new Script();
        version.setScript(script);
        version.setConfig("<xml-config>");

        JenkinsInfo info = JenkinsInfo.builder()
                .uri("http://jenkins.local")
                .build();

        HttpHeaders headers = new HttpHeaders();
        when(httpClientService.buildHeaders(eq(info), any(MediaType.class)))
                .thenReturn(headers);

        compensationService.reCreateJob(version, info, "test-job");

        // DB 롤백
        verify(stageService).updateStages(eq(version), eq(script));
        verify(pipelineVersionRepository).save(version);

        // Jenkins 롤백
        verify(httpClientService).exchange(
                eq("http://jenkins.local/createItem?name=test-job"),
                eq(org.springframework.http.HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("rollbackPipelineLatestVersion - 이전 스냅샷 데이터를 기준으로 최신 버전 필드를 덮어쓴다")
    void rollbackPipelineLatestVersion_shouldCopyFieldsAndSave() {
        Script script = new Script();
        PipelineVersion latest = new PipelineVersion();
        PipelineVersion previous = new PipelineVersion();
        previous.setDescription("desc");
        previous.setIsTriggered(true);
        previous.setSchedule("0 0 * * *");
        previous.setConfig("<xml>");
        previous.setScript(script);

        compensationService.rollbackPipelineLatestVersion(latest, previous);

        assertThat(latest.getDescription()).isEqualTo("desc");
        assertThat(latest.getIsTriggered()).isTrue();
        assertThat(latest.getSchedule()).isEqualTo("0 0 * * *");
        assertThat(latest.getConfig()).isEqualTo("<xml>");
        assertThat(latest.getScript()).isEqualTo(script);
        verify(stageService).updateStages(latest, script);
        verify(pipelineVersionRepository).save(latest);
    }
}
