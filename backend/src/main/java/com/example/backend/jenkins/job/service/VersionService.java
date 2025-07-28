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

    // 특정 파이프라인버전 삭제
    // 조건: 가장 최신 버전은 삭제 할 수 없음
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

    @Transactional
    public void snapshotVersion(UUID pipelineId, String snapshotName) {
        Pipeline pipeline = pipelineService.getPipelineById(pipelineId);
        PipelineVersion version = pipelineService.getLatestVersion(pipeline);

        PipelineVersion snapshot = PipelineVersion.replicateEntity(version, snapshotName);

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

    public PipelineVersion getPipelineVersionById(UUID pipelineVersionId) {
        return pipelineVersionRepository.findWithPipelineById(pipelineVersionId)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_VERSION_NOT_FOUND));
    }

    public boolean isOwner(Users user, UUID pipelineVersionId) {
        PipelineVersion pipelineVersion = pipelineVersionRepository.findWithPipelineAndJenkinsInfoAndUserById(pipelineVersionId)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_VERSION_NOT_FOUND));
        return user.getId().equals(pipelineVersion.getPipeline().getJenkinsInfo().getUser().getId());
    }
}
