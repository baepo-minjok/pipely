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

    /**
     * Creates Stage entities and VersionStage associations for a given PipelineVersion and Script.
     * Extracts stage names from the script, ensures Stage entities exist, and links them to the PipelineVersion.
     *
     * @param pipelineVersion the PipelineVersion to associate stages with
     * @param script          the Script containing stage definitions
     */
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

    /**
     * Updates stages of an existing PipelineVersion.
     * Clears previous stage associations and recreates them based on the new Script.
     *
     * @param pipelineVersion the PipelineVersion to update
     * @param script          the Script containing the updated stage definitions
     */
    @Transactional
    public void updateStages(PipelineVersion pipelineVersion, Script script) {
        pipelineVersion.getStageList().clear();
        versionStageRepository.deleteVersionStageByPipelineVersion(pipelineVersion);
        createStages(pipelineVersion, script);
    }

    /**
     * Extracts stage names from the script content.
     * Converts names to uppercase and replaces non-word characters with underscores for consistency.
     *
     * @param script the Script entity
     * @return a list of normalized stage names
     */
    private List<String> extractStageNames(Script script) {
        if (script == null) return Collections.emptyList();
        return scriptEditUtil.extractStageNames(script.getScript())
                .stream()
                .map(s -> s.toUpperCase().replaceAll("\\W+", "_"))
                .collect(Collectors.toList());
    }
}
