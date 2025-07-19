package com.example.backend.jenkins.job.service;

import com.example.backend.jenkins.job.model.PipelineVersion;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.Stage;
import com.example.backend.jenkins.job.model.VersionStage;
import com.example.backend.jenkins.job.repository.StageRepository;
import com.example.backend.jenkins.job.repository.VersionStageRepository;
import com.example.backend.util.ScriptEditUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StageService {

    private final StageRepository stageRepository;
    private final VersionStageRepository versionStageRepository;
    private final ScriptEditUtil scriptEditUtil;

    @Transactional
    public void createStages(PipelineVersion pipelineVersion, Script script) {
        List<String> names = extractStageNames(script);

        for (int i = 0; i < names.size(); i++) {
            String stageName = names.get(i);

            Stage stage = stageRepository.findById(stageName)
                    .orElseGet(() -> {
                        Stage s = Stage.builder()
                                .name(stageName)
                                .build();
                        return stageRepository.save(s);   // 새로 저장
                    });

            VersionStage versionStage = VersionStage.builder()
                    .orderIndex(i)
                    .stage(stage)
                    .pipelineVersion(pipelineVersion)
                    .build();
            versionStageRepository.save(versionStage);

            pipelineVersion.getStageList().add(versionStage);
        }
    }

    @Transactional
    public void updateStages(PipelineVersion pipelineVersion, Script script) {
        pipelineVersion.getStageList().clear();
        versionStageRepository.deleteVersionStageByPipelineVersion(pipelineVersion);
        createStages(pipelineVersion, script);
    }

    private List<String> extractStageNames(Script script) {
        if (script == null) return Collections.emptyList();
        return scriptEditUtil.extractStageNames(script.getScript())
                .stream()
                .map(s -> s.toUpperCase().replaceAll("\\W+", "_"))
                .collect(Collectors.toList());
    }
}
