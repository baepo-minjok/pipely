package com.example.backend.jenkins.job.service;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.PipelineVersion;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.VersionStage;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.jenkins.job.repository.PipelineVersionRepository;
import com.example.backend.jenkins.job.repository.ScriptRepository;
import com.example.backend.service.HttpClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VersionService {

    private final StageService stageService;
    private final PipelineService pipelineService;
    private final HttpClientService httpClientService;
    private final PipelineRepository pipelineRepository;
    private final CompensationService compensationService;
    private final PipelineVersionRepository pipelineVersionRepository;
    private final ScriptRepository scriptRepository;

    /**
     * Deletes a specific PipelineVersion.
     * Condition: The latest version cannot be deleted.
     *
     * @param pipelineVersionId the UUID of the PipelineVersion to delete
     * @throws CustomException if attempting to delete the latest version
     */
    @Transactional
    public void deletePipelineVersion(UUID pipelineVersionId) {

        PipelineVersion pipelineVersion = getPipelineVersionById(pipelineVersionId);
        Pipeline pipeline = pipelineVersion.getPipeline();

        // 최신 버전은 삭제 불가
        if (pipeline.getLatestVersionId().equals(pipelineVersionId)) {
            throw new CustomException(ErrorCode.CANNOT_DELETE_LATEST_VERSION);
        }

        List<PipelineVersion> existingVersions = pipeline.getVersionList();

        existingVersions.remove(pipelineVersion);

    }

    /**
     * Rolls back the latest PipelineVersion to a specified snapshot version.
     *
     * @param snapshotVersionId the UUID of the snapshot PipelineVersion
     */
    @Transactional
    public void rollbackToSnapshot(UUID snapshotVersionId) {
        PipelineVersion target = getPipelineVersionById(snapshotVersionId);
        Pipeline pipeline = target.getPipeline();

        JenkinsInfo info = pipeline.getJenkinsInfo();
        PipelineVersion previousVersion = pipelineService.getLatestVersion(pipeline);
        PipelineVersion latestVersion = pipelineService.getLatestVersion(pipeline);

        String config = target.getConfig();
        Script script = target.getScript();

        latestVersion.setDescription(target.getDescription());
        latestVersion.setIsTriggered(target.getIsTriggered());
        latestVersion.setSchedule(target.getSchedule());
        latestVersion.setConfig(config);
        latestVersion.setScript(script);

        stageService.updateStages(latestVersion, script);

        //snapshot 버전으로 변경
        PipelineVersion savedPipelineVersion = pipelineVersionRepository.save(latestVersion);
        pipelineVersionRepository.flush();

        httpClientService.callJenkins(info.getUri() + "/job/" + pipeline.getName() + "/config.xml",
                config, info, HttpMethod.POST, () -> compensationService.rollbackPipelineLatestVersion(savedPipelineVersion, previousVersion));

    }

    /**
     * Creates a snapshot of the latest PipelineVersion.
     * A new PipelineVersion entity is created along with a replicated Script and Stage list.
     *
     * @param pipelineId   the UUID of the Pipeline to snapshot
     * @param snapshotName the name for the new snapshot version
     */
    @Transactional
    public void snapshotVersion(UUID pipelineId, String snapshotName) {
        Pipeline pipeline = pipelineService.getPipelineById(pipelineId);
        PipelineVersion version = pipelineService.getLatestVersion(pipeline);

        Script script = Script.replicateEntity(version.getScript());
        script = scriptRepository.save(script);

        PipelineVersion snapshot = PipelineVersion.replicateEntity(version, snapshotName);

        snapshot.setScript(script);

        List<VersionStage> copiedStages = new ArrayList<>();
        for (VersionStage orig : version.getStageList()) {
            VersionStage copied = VersionStage.builder()
                    .orderIndex(orig.getOrderIndex())
                    .stage(orig.getStage())
                    .pipelineVersion(snapshot)
                    .build();
            copiedStages.add(copied);
        }
        snapshot.setStageList(copiedStages);

        pipelineVersionRepository.save(snapshot);

        pipeline.getVersionList().add(snapshot);
        pipelineRepository.save(pipeline);
    }

    /**
     * Retrieves a PipelineVersion by its ID.
     *
     * @param pipelineVersionId the UUID of the PipelineVersion
     * @return the PipelineVersion entity
     * @throws CustomException if the version is not found
     */
    public PipelineVersion getPipelineVersionById(UUID pipelineVersionId) {
        return pipelineVersionRepository.findWithPipelineById(pipelineVersionId)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_VERSION_NOT_FOUND));
    }

    /**
     * Checks whether a given user is the owner of a PipelineVersion.
     *
     * @param user              the Users entity
     * @param pipelineVersionId the UUID of the PipelineVersion
     * @return true if the user is the owner, false otherwise
     */
    public boolean isOwner(Users user, UUID pipelineVersionId) {
        PipelineVersion pipelineVersion = pipelineVersionRepository.findWithPipelineAndJenkinsInfoAndUserById(pipelineVersionId)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_VERSION_NOT_FOUND));
        return user.getId().equals(pipelineVersion.getPipeline().getJenkinsInfo().getUser().getId());
    }
}
