package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.model.*;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.jenkins.job.repository.PipelineVersionRepository;
import com.example.backend.service.HttpClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VersionServiceTest {

    @InjectMocks
    private VersionService versionService;

    @Mock
    private StageService stageService;
    @Mock private PipelineService pipelineService;
    @Mock private HttpClientService httpClientService;
    @Mock private PipelineRepository pipelineRepository;
    @Mock private CompensationService compensationService;
    @Mock private PipelineVersionRepository pipelineVersionRepository;

    private UUID pipelineId;
    private UUID versionId;

    @BeforeEach
    void setUp() {
        pipelineId = UUID.randomUUID();
        versionId = UUID.randomUUID();
    }

    //버전 삭제
    //조건: 최신 버전 삭제 -> 예외
    @DisplayName("deletePipelineVersion - 최신 버전 삭제 시 예외 발생")
    @Test
    void deletePipelineVersion_shouldThrowException_whenDeletingLatestVersion() {
        UUID versionId = UUID.randomUUID();
        Pipeline pipeline = new Pipeline();
        pipeline.setLatestVersionId(versionId); // 이 버전이 최신

        PipelineVersion version = PipelineVersion.builder()
                .id(versionId)
                .pipeline(pipeline)
                .build();

        when(pipelineVersionRepository.findWithPipelineById(versionId)).thenReturn(Optional.of(version));

        // 예외 발생 검증
        assertThatThrownBy(() -> versionService.deletePipelineVersion(versionId))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CANNOT_DELETE_LATEST_VERSION);
                });

    }


    // 버전 삭제 성공
    @Test
    @DisplayName("deletePipelineVersion - 최신 버전이 아닌 경우 정상 삭제")
    void deletePipelineVersion_shouldRemoveVersion_whenNotLatestVersion() {
        UUID latestVersionId = UUID.randomUUID();
        UUID deletingId = UUID.randomUUID();

        Pipeline pipeline = new Pipeline();
        pipeline.setLatestVersionId(latestVersionId);

        PipelineVersion versionToDelete = PipelineVersion.builder()
                .id(deletingId)
                .pipeline(pipeline)
                .build();

        List<PipelineVersion> versionList = new ArrayList<>();
        versionList.add(versionToDelete);

        pipeline.setVersionList(versionList);

        when(pipelineVersionRepository.findWithPipelineById(deletingId))
                .thenReturn(Optional.of(versionToDelete));

        versionService.deletePipelineVersion(deletingId);

        assertThat(pipeline.getVersionList()).doesNotContain(versionToDelete);
    }



    @Test
    @DisplayName("snapshotVersion - 최신 버전을 기반으로 스냅샷 생성 및 저장")

    void snapshotVersion_shouldSaveSnapshotCorrectly() {
        // 1. pipelineService.getLatestVersion() 으로 최신 버전 조회
        // 2. 기존 VersionStage 목록을 복사해서 새 PipelineVersion 생성
        // 3. pipeline.getVersionList()에 추가 및 저장

        UUID pipelineId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();

        Pipeline pipeline = Pipeline.builder()
                .id(pipelineId)
                .versionList(new ArrayList<>())
                .build();

        Stage stage1 = Stage.builder().name("BUILD").build();
        Stage stage2 = Stage.builder().name("TEST").build();

        VersionStage versionStage1 = VersionStage.builder()
                .orderIndex(1)
                .stage(stage1)
                .build();

        VersionStage versionStage2 = VersionStage.builder()
                .orderIndex(2)
                .stage(stage2)
                .build();

        PipelineVersion latestVersion = PipelineVersion.builder()
                .id(versionId)
                .pipeline(pipeline)
                .stageList(List.of(versionStage1, versionStage2))
                .build();

        when(pipelineService.getPipelineById(pipelineId)).thenReturn(pipeline);
        when(pipelineService.getLatestVersion(pipeline)).thenReturn(latestVersion);
        when(pipelineVersionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // 스냅샷 저장 시 save()가 호출되고, pipeline 버전 리스트에 추가되었는지 확인
        versionService.snapshotVersion(pipelineId, "snapshot-v1");

        verify(pipelineVersionRepository).save(any());
        verify(pipelineRepository).save(pipeline);
        assertThat(pipeline.getVersionList()).hasSize(1);
    }


    @Test
    @DisplayName("rollbackToSnapshot - 스냅샷 기준으로 최신 버전 되돌리기 + Jenkins 반영")
    void rollbackToSnapshot_shouldUpdateConfigAndCallJenkins() {
        // 1. 대상 snapshotVersion을 조회
        // 2. 최신 버전의 config 등 필드들을 snapshot 기준으로 덮어씀
        // 3. stageService.updateStages 호출
        // 4. jenkins 서버에 config.xml 재전송

        JenkinsInfo info = new JenkinsInfo();
        Pipeline pipeline = Pipeline.builder()
                .id(pipelineId)
                .jenkinsInfo(info)
                .name("test-job")
                .build();

        // 되돌릴 snapshot
        PipelineVersion targetSnapshot = PipelineVersion.builder()
                .id(versionId)
                .pipeline(pipeline)
                .config("<xml>snapshot</xml>")
                .schedule("0 10 * * *")
                .description("desc")
                .isTriggered(true)
                .build();

        // 최신 버전
        PipelineVersion latestVersion = PipelineVersion.builder()
                .id(UUID.randomUUID())
                .pipeline(pipeline)
                .build();

        Script script = new Script();
        targetSnapshot.setScript(script);

        when(pipelineVersionRepository.findWithPipelineById(versionId)).thenReturn(Optional.of(targetSnapshot));
        when(pipelineService.getLatestVersion(pipeline)).thenReturn(latestVersion);
        when(pipelineVersionRepository.save(any())).thenReturn(latestVersion);

        // rollback 시 stage 업데이트와 Jenkins config가 정상적으로 호출이 되는지 확인
        versionService.rollbackToSnapshot(versionId);

        verify(stageService).updateStages(latestVersion, script);
        verify(httpClientService).callJenkins(
                contains("/job/test-job/config.xml"),
                eq("<xml>snapshot</xml>"),
                eq(info),
                eq(HttpMethod.POST),
                any()
        );
    }

}
