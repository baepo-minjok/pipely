package com.example.backend.jenkins.job.service;

import com.example.backend.auth.user.model.Users;
import com.example.backend.event.PipelineStateChangedEvent;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.PipelineVersion;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.jenkins.job.repository.PipelineVersionRepository;
import com.example.backend.jenkins.notification.service.JobNotificationService;
import com.example.backend.service.HttpClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PipelineService {

    private final HttpClientService httpClientService;
    private final JenkinsInfoService jenkinsInfoService;
    private final ConfigService configService;
    private final ScriptService scriptService;
    private final PipelineRepository pipelineRepository;
    private final CompensationService compensationService;
    private final StageService stageService;
    private final PipelineVersionRepository pipelineVersionRepository;
    private final JobNotificationService jobNotificationService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Create a new Jenkins job and persist the pipeline.
     */
    @Transactional
    public void createJob(RequestDto.CreateDto dto) {
        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(dto.getInfoId());
        ensureUniqueName(info.getId(), dto.getName());

        Script script = loadScript(dto.getScriptId());
        String config = buildConfig(dto, script);
        String name = "Initial Version";

        //파이프라인 & 파이프라인 버전 저장
        Pipeline pipeline = savePipeline(dto, info, script, config, name);

        httpClientService.callJenkins(info.getUri() + "/createItem?name=" + dto.getName(),
                config, info, HttpMethod.POST,
                () -> compensationService.deletePipeline(pipeline.getId()));
    }

    /**
     * Update an existing Jenkins job or recreate if renamed.
     */
    @Transactional
    public void updateJob(RequestDto.UpdateDto dto) {
        Pipeline pipeline = getPipelineById(dto.getPipelineId());
        PipelineVersion version = getPipelineVersionById(pipeline.getLatestVersionId());

        JenkinsInfo info = pipeline.getJenkinsInfo();
        String preName = pipeline.getName();
        boolean isRenamed = isRenamed(preName, dto);

        Script script = loadScript(dto.getScriptId());
        String config = buildConfig(dto, script);
        applyPipelineChanges(pipeline, dto, script, config);

        if (isRenamed) {    // 수정할 Job 이름이 다를 때
            // 이미 존재하는 이름인지 검사
            ensureUniqueName(info.getId(), dto.getName(), pipeline);

            // 기존 job 삭제 요청
            httpClientService.deleteJobOnJenkins(info, preName,
                    () -> compensationService.rollback(version));
            // 생성 요청
            httpClientService.callJenkins(info.getUri() + "/createItem?name=" + dto.getName(),
                    config, info, HttpMethod.POST,
                    () -> compensationService.reCreateJob(version, info, preName));
        } else {        // 수정할 Job 이름이 같을 때
            // 수정 요청
            httpClientService.callJenkins(info.getUri() + "/job/" + dto.getName() + "/config.xml",
                    config, info, HttpMethod.POST, () -> compensationService.rollback(version));
        }
    }

    public Pipeline getPipelineById(UUID id) {
        return pipelineRepository.findWithInfoAndScriptById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_NOT_FOUND));
    }

    @Transactional
    public void softDeletePipelineById(UUID id) {
        Pipeline pipeline = getPipelineById(id);
        pipeline.setDeletedAt(LocalDateTime.now());
        pipeline.setIsDeleted(true);
        pipelineRepository.save(pipeline);
        httpClientService.deleteJobOnJenkins(pipeline.getJenkinsInfo(), pipeline.getName(), () -> compensationService.softDeletePipeline(pipeline, null, false));
    }

    @Transactional
    public void hardDeletePipelineById(UUID id) {
        pipelineRepository.delete(getPipelineById(id));
    }

    public List<ResponseDto.LightJobDto> getLightJobs(UUID jenkinsInfoId) {
        return pipelineRepository.findActiveWithScriptByJenkinsInfoId(jenkinsInfoId).stream()
                .map(ResponseDto::entityToLightJobDto)
                .collect(Collectors.toList());
    }

    public List<ResponseDto.LightJobDto> getDeletedLightJobs(UUID jenkinsInfoId) {
        return pipelineRepository.findDeletedWithScriptByJenkinsInfoId(jenkinsInfoId).stream()
                .map(ResponseDto::entityToLightJobDto)
                .collect(Collectors.toList());
    }

    public ResponseDto.DetailJobDto getDetailJob(UUID jobId) {
        Pipeline pipeline = getPipelineById(jobId);
        return ResponseDto.entityToDetailJobDto(pipeline);
    }

    public boolean isOwner(Users user, UUID pipelineId) {
        Pipeline pipeline = pipelineRepository.findWithInfoAndScriptAndUserById(pipelineId)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_NOT_FOUND));
        return user.getId().equals(pipeline.getJenkinsInfo().getUser().getId());
    }

    private void ensureUniqueName(UUID infoId, String name) {
        pipelineRepository.findByJenkinsInfoIdAndName(infoId, name)
                .ifPresent(p -> {
                    throw new CustomException(ErrorCode.JENKINS_JOB_EXIST);
                });
    }

    private void ensureUniqueName(UUID infoId, String name, Pipeline current) {
        pipelineRepository.findByJenkinsInfoIdAndName(infoId, name)
                .filter(p -> !p.getId().equals(current.getId()))
                .ifPresent(p -> {
                    throw new CustomException(ErrorCode.JENKINS_JOB_EXIST);
                });
    }

    private boolean isRenamed(String previousName, RequestDto.UpdateDto dto) {
        return !previousName.equals(dto.getName());
    }

    private Script loadScript(UUID scriptId) {
        return scriptId != null ? scriptService.getScriptById(scriptId) : null;
    }

    private String buildConfig(RequestDto.BaseDto dto, Script script) {
        return configService.createConfig(
                configService.buildConfigContext(dto, script)
        );
    }

    private Pipeline savePipeline(RequestDto.CreateDto dto, JenkinsInfo info, Script script, String config, String name) {
        Pipeline pipeline = RequestDto.toEntity(dto, info);
        Pipeline saved = pipelineRepository.save(pipeline);
        UUID latestVersionId = saveVersion(saved, script, config, dto, name);

        pipeline.setLatestVersionId(latestVersionId);
        saved = pipelineRepository.save(pipeline);
        pipelineRepository.flush();
        return saved;
    }

    private void applyPipelineChanges(Pipeline pipeline, RequestDto.UpdateDto dto, Script script, String config) {
        pipeline.setName(dto.getName());
        pipeline.setUpdatedAt(LocalDateTime.now());

        updateVersion(getLatestVersion(pipeline), dto, script, config);

        pipelineRepository.save(pipeline);
        pipelineRepository.flush();
    }

    @Transactional
    public void restorationJob(UUID pipelineId) {
        Pipeline pipeline = getPipelineById(pipelineId);
        PipelineVersion pipelineVersion = getLatestVersion(pipeline);
        JenkinsInfo info = pipeline.getJenkinsInfo();
        LocalDateTime time = pipeline.getDeletedAt();

        ensureUniqueName(info.getId(), pipeline.getName(), pipeline);

        compensationService.softDeletePipeline(pipeline, null, false);

        String config = pipelineVersion.getConfig();

        httpClientService.callJenkins(info.getUri() + "/createItem?name=" + pipeline.getName(),
                config, info, HttpMethod.POST,
                () -> compensationService.softDeletePipeline(pipeline, time, true));
    }

    public PipelineVersion getLatestVersion(Pipeline pipeline) {
        return pipelineVersionRepository.findWithScriptAndStageListById(pipeline.getLatestVersionId())
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_VERSION_NOT_FOUND));
    }

    public PipelineVersion getPipelineVersionById(UUID pipelineVersionId) {
        return pipelineVersionRepository.findById(pipelineVersionId)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_VERSION_NOT_FOUND));
    }

    //새 버전 저장
    private UUID saveVersion(Pipeline pipeline, Script script, String config, RequestDto.CreateDto dto, String name) {

        PipelineVersion version = PipelineVersion.builder()
                .name(name)
                .description(dto.getDescription())
                .isTriggered(dto.getTrigger())
                .schedule(dto.getSchedule())
                .createdAt(LocalDateTime.now())
                .config(config)
                .script(script)
                .pipeline(pipeline)
                .build();
        PipelineVersion savedVersion = pipelineVersionRepository.save(version);

        jobNotificationService.saveJobNotifications(dto.getNotificationMap(), savedVersion);

        stageService.createStages(savedVersion, script);

        pipeline.getVersionList().add(version);

        pipelineVersionRepository.flush();
        return savedVersion.getId();
    }

    private void updateVersion(PipelineVersion pipelineVersion, RequestDto.UpdateDto dto, Script script, String config) {

        pipelineVersion.setDescription(dto.getDescription());
        pipelineVersion.setIsTriggered(dto.getTrigger());
        pipelineVersion.setSchedule(dto.getSchedule());
        pipelineVersion.setConfig(config);
        pipelineVersion.setScript(script);

        jobNotificationService.updateJobNotifications(dto.getNotificationMap(), pipelineVersion);

        stageService.updateStages(pipelineVersion, script);

        pipelineVersionRepository.save(pipelineVersion);
        pipelineVersionRepository.flush();
    }

    @Transactional
    public void setState(RequestDto.StatusDto dto) {
        Pipeline pipeline = getPipelineById(dto.getJobId());
        String state = "";
        if (dto.isSuccess()) {
            pipeline.setBuildState(Pipeline.BuildState.BUILD_SUCCESS);
            state = "SUCCESS";
        } else {
            pipeline.setBuildState(Pipeline.BuildState.BUILD_FAILURE);
            state = "FAILURE";
        }
        pipelineRepository.save(pipeline);

        eventPublisher.publishEvent(new PipelineStateChangedEvent(pipeline.getJenkinsInfo().getUser().getEmail(),
                PipelineStateChangedEvent.ChangedState.builder().name(pipeline.getName()).state(state).build()));
    }

    @Transactional
    public void setStatusPending(Pipeline pipeline) {
        pipeline.setLatestBuildTime(LocalDateTime.now());
        pipeline.setBuildState(Pipeline.BuildState.BUILD_RUNNING);
        pipelineRepository.save(pipeline);
    }

    public List<ResponseDto.PipelineVersionDto> getLightVersionDtoList(UUID pipelineId) {
        Pipeline pipeline = getPipelineById(pipelineId);
        return ResponseDto.toListOfPipelineVersionDtos(pipeline.getVersionList());
    }

    @Transactional
    public void renameVersion(RequestDto.RenameDto dto) {
        PipelineVersion pipelineVersion = getPipelineVersionById(dto.getPipelineId());
        pipelineVersion.setName(dto.getNewName());
        pipelineVersionRepository.save(pipelineVersion);
    }

    @Transactional
    public void setState(Pipeline pipeline, String state) {
        pipeline.setBuildState(Pipeline.BuildState.valueOf("BUILD_" + state));
        pipelineRepository.save(pipeline);
    }
}

