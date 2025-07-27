package com.example.backend.jenkins.job.service;


import com.example.backend.jenkins.job.model.PipelineVersion;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.Stage;
import com.example.backend.jenkins.job.model.VersionStage;
import com.example.backend.jenkins.job.repository.StageRepository;
import com.example.backend.jenkins.job.repository.VersionStageRepository;
import com.example.backend.util.ScriptEditUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class StageServiceTest {

    @InjectMocks
    private StageService stageService;

    @Mock
    private StageRepository stageRepository;

    @Mock
    private VersionStageRepository versionStageRepository;

    @Mock
    private ScriptEditUtil scriptEditUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * [StageService 흐름 요약]
     * 1. Script로부터 stage 이름 목록을 추출한다 (extractStageNames)
     * 2. 각 stage 이름에 대해 Stage가 DB에 존재하는지 확인
     *    - 존재하지 않으면 Stage 엔티티를 새로 생성해 저장
     * 3. 각 Stage와 PipelineVersion을 연결하는 VersionStage를 생성해 저장
     * 4. PipelineVersion.stageList에 연결 정보를 추가
     */
    @Test
    @DisplayName("createStages - Stage가 존재하지 않으면 새로 생성하고 VersionStage로 연결한다")
    void createStages_shouldCreateNewStagesAndVersionStages() {
        PipelineVersion pipelineVersion = PipelineVersion.builder()
                .stageList(new ArrayList<>())
                .build();

        Script script = Script.builder().script("pipeline").build();
        List<String> extractedNames = Arrays.asList("Build", "Deploy");

        //script로부터 추출된 stage들이 모두 신규일 때
        when(scriptEditUtil.extractStageNames("pipeline")).thenReturn(extractedNames);
        when(stageRepository.findById(anyString())).thenReturn(Optional.empty());
        when(stageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(versionStageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // stage, VersionStage 각각 저장되고 pipelineVersion.stageList에도 추가됨
        stageService.createStages(pipelineVersion, script);

        assertThat(pipelineVersion.getStageList()).hasSize(2);
        verify(stageRepository, times(2)).save(any(Stage.class));
        verify(versionStageRepository, times(2)).save(any(VersionStage.class));
    }

    // 조건: script에서 추출된 stage 이름들이 DB에 모두 존재하지 않는 경우
    @Test
    @DisplayName("createStages - Stage가 이미 존재하는 경우 새로 저장하지 않고 재사용한다")
    void createStages_shouldUseExistingStageIfFound() {
        PipelineVersion version = PipelineVersion.builder().stageList(new ArrayList<>()).build();
        Script script = Script.builder().script("test").build();
        String stageName = "TestStage";

        // StageRepository에서 해당 이름을 찾음 → 저장 불필요
        when(scriptEditUtil.extractStageNames("test")).thenReturn(List.of(stageName));
        when(stageRepository.findById("TESTSTAGE")).thenReturn(Optional.of(Stage.builder().name("TESTSTAGE").build()));
        when(versionStageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        stageService.createStages(version, script);

        verify(stageRepository, never()).save(any()); // 저장 안함 (존재)
        verify(versionStageRepository).save(any());
    }


    //조건: pipelineVersion에 기존 stageList가 있고 새롭게 stage 수정하는 경우
    @Test
    @DisplayName("updateStages - 기존 연결을 삭제하고 새로운 Stage들로 다시 생성한다")
    void updateStages_shouldClearAndRecreateStages() {
        PipelineVersion version = PipelineVersion.builder().stageList(new ArrayList<>()).build();
        Script script = Script.builder().script("script").build();

        when(scriptEditUtil.extractStageNames("script")).thenReturn(List.of("StageOne"));
        when(stageRepository.findById(any())).thenReturn(Optional.empty());
        when(stageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(versionStageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        stageService.updateStages(version, script);

        verify(versionStageRepository).deleteVersionStageByPipelineVersion(version);
        verify(versionStageRepository).save(any());
        assertThat(version.getStageList()).hasSize(1);
    }
}

