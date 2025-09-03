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
    private final PipelineRepository pipelineRepository;
    private final CompensationService compensationService;
    private final StageService stageService;
    private final PipelineVersionRepository pipelineVersionRepository;
    private final JobNotificationService jobNotificationService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Create a new Jenkins job and persist the pipeline.
     *
     * @param dto    DTO containing pipeline creation request data
     * @param script Script entity containing the build pipeline definition
     */
    @Transactional
    public void createJob(RequestDto.CreateDto dto, Script script) {
        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(dto.getInfoId());
        ensureUniqueName(info.getId(), dto.getName());

        String config = buildConfig(dto, script);
        String name = "Initial Version";

        Pipeline pipeline = savePipeline(dto, info, script, config, name);

        httpClientService.callJenkins(info.getUri() + "/createItem?name=" + dto.getName(),
                config, info, HttpMethod.POST,
                () -> compensationService.deletePipeline(pipeline.getId()));
    }

    /**
     * Update an existing Jenkins job or recreate if renamed.
     *
     * @param dto    DTO containing pipeline update request data
     * @param script Script entity containing the updated build pipeline definition
     */
    @Transactional
    public void updateJob(RequestDto.UpdateDto dto, Script script) {
        Pipeline pipeline = getPipelineById(dto.getPipelineId());
        PipelineVersion version = getPipelineVersionById(pipeline.getLatestVersionId());

        JenkinsInfo info = pipeline.getJenkinsInfo();
        String preName = pipeline.getName();
        boolean isRenamed = isRenamed(preName, dto);

        String config = buildConfig(dto, script);
        applyPipelineChanges(pipeline, dto, script, config);

        if (isRenamed) {
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

    /**
     * Retrieve a pipeline by its ID.
     *
     * @param id Pipeline ID
     * @return Pipeline entity with Jenkins info and script
     */
    public Pipeline getPipelineById(UUID id) {
        return pipelineRepository.findWithInfoAndScriptById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_NOT_FOUND));
    }

    /**
     * Soft delete a pipeline (mark as deleted without removing DB entry).
     * Also requests job deletion on Jenkins.
     *
     * @param id Pipeline ID
     */
    @Transactional
    public void softDeletePipelineById(UUID id) {
        Pipeline pipeline = getPipelineById(id);
        pipeline.setDeletedAt(LocalDateTime.now());
        pipeline.setIsDeleted(true);
        pipelineRepository.save(pipeline);
        httpClientService.deleteJobOnJenkins(pipeline.getJenkinsInfo(), pipeline.getName(), () -> compensationService.softDeletePipeline(pipeline, null, false));
    }

    /**
     * Hard delete a pipeline (completely remove from DB).
     *
     * @param id Pipeline ID
     */
    @Transactional
    public void hardDeletePipelineById(UUID id) {
        pipelineRepository.delete(getPipelineById(id));
    }

    /**
     * Retrieve a list of active pipelines (lightweight DTOs).
     *
     * @param jenkinsInfoId Jenkins info ID
     * @return List of light job DTOs
     */
    public List<ResponseDto.LightJobDto> getLightJobs(UUID jenkinsInfoId) {
        return pipelineRepository.findActiveWithScriptByJenkinsInfoId(jenkinsInfoId).stream()
                .map(ResponseDto::entityToLightJobDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a list of deleted pipelines (lightweight DTOs).
     *
     * @param jenkinsInfoId Jenkins info ID
     * @return List of deleted light job DTOs
     */
    public List<ResponseDto.LightJobDto> getDeletedLightJobs(UUID jenkinsInfoId) {
        return pipelineRepository.findDeletedWithScriptByJenkinsInfoId(jenkinsInfoId).stream()
                .map(ResponseDto::entityToLightJobDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve detailed information about a pipeline.
     *
     * @param jobId Pipeline ID
     * @return Detail job DTO containing full pipeline information
     */
    public ResponseDto.DetailJobDto getDetailJob(UUID jobId) {
        Pipeline pipeline = getPipelineById(jobId);
        return ResponseDto.entityToDetailJobDto(pipeline);
    }

    /**
     * Check if a user is the owner of a pipeline.
     *
     * @param user       User entity
     * @param pipelineId Pipeline ID
     * @return true if the user is the owner, false otherwise
     */
    public boolean isOwner(Users user, UUID pipelineId) {
        Pipeline pipeline = pipelineRepository.findWithInfoAndScriptAndUserById(pipelineId)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_NOT_FOUND));
        return user.getId().equals(pipeline.getJenkinsInfo().getUser().getId());
    }

    /**
     * Ensure that no pipeline with the same name exists under a given JenkinsInfo.
     *
     * @param infoId JenkinsInfo identifier
     * @param name   proposed pipeline name
     * @throws CustomException if a pipeline with the same name already exists
     */
    private void ensureUniqueName(UUID infoId, String name) {
        pipelineRepository.findByJenkinsInfoIdAndName(infoId, name)
                .ifPresent(p -> {
                    throw new CustomException(ErrorCode.JENKINS_JOB_EXIST);
                });
    }

    /**
     * Ensure that no other pipeline (excluding the current one) has the same name under a given JenkinsInfo.
     *
     * @param infoId  JenkinsInfo identifier
     * @param name    proposed pipeline name
     * @param current the current Pipeline being updated
     * @throws CustomException if another pipeline with the same name exists
     */
    private void ensureUniqueName(UUID infoId, String name, Pipeline current) {
        pipelineRepository.findByJenkinsInfoIdAndName(infoId, name)
                .filter(p -> !p.getId().equals(current.getId()))
                .ifPresent(p -> {
                    throw new CustomException(ErrorCode.JENKINS_JOB_EXIST);
                });
    }

    /**
     * Check whether a pipeline name has changed during an update.
     *
     * @param previousName the original pipeline name
     * @param dto          DTO containing updated job details (including new name)
     * @return true if the name has changed, false otherwise
     */
    private boolean isRenamed(String previousName, RequestDto.UpdateDto dto) {
        return !previousName.equals(dto.getName());
    }

    /**
     * Build the Jenkins job configuration XML based on the request DTO and script.
     *
     * @param dto    base DTO containing pipeline configuration data
     * @param script associated Script entity
     * @return generated Jenkins job configuration (XML string)
     */
    private String buildConfig(RequestDto.BaseDto dto, Script script) {
        return configService.createConfig(
                configService.buildConfigContext(dto, script)
        );
    }

    /**
     * Persist a new Pipeline entity and its initial version.
     *
     * @param dto    DTO containing pipeline creation request details
     * @param info   associated JenkinsInfo entity
     * @param script Script entity associated with this pipeline
     * @param config generated Jenkins job configuration (XML string)
     * @param name   the display name of the version (e.g., "Initial Version")
     * @return the saved Pipeline entity with its latest version set
     */
    private Pipeline savePipeline(RequestDto.CreateDto dto, JenkinsInfo info, Script script, String config, String name) {
        Pipeline pipeline = RequestDto.toEntity(dto, info);
        Pipeline saved = pipelineRepository.save(pipeline);
        UUID latestVersionId = saveVersion(saved, script, config, dto, name);

        pipeline.setLatestVersionId(latestVersionId);
        saved = pipelineRepository.save(pipeline);
        pipelineRepository.flush();
        return saved;
    }

    /**
     * Apply updates to an existing Pipeline and its latest version.
     *
     * @param pipeline the Pipeline entity to update
     * @param dto      DTO containing updated job details
     * @param script   updated Script entity
     * @param config   new Jenkins job configuration (XML string)
     */
    private void applyPipelineChanges(Pipeline pipeline, RequestDto.UpdateDto dto, Script script, String config) {
        pipeline.setName(dto.getName());
        pipeline.setUpdatedAt(LocalDateTime.now());

        updateVersion(getLatestVersion(pipeline), dto, script, config);

        pipelineRepository.save(pipeline);
        pipelineRepository.flush();
    }

    /**
     * Restore a previously deleted pipeline on Jenkins.
     *
     * @param pipelineId Pipeline ID
     */
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

    /**
     * Retrieve the latest pipeline version.
     *
     * @param pipeline Pipeline entity
     * @return Latest PipelineVersion entity
     */
    public PipelineVersion getLatestVersion(Pipeline pipeline) {
        return pipelineVersionRepository.findWithScriptAndStageListById(pipeline.getLatestVersionId())
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_VERSION_NOT_FOUND));
    }

    /**
     * Retrieve a pipeline version by its ID.
     *
     * @param pipelineVersionId PipelineVersion ID
     * @return PipelineVersion entity
     */
    public PipelineVersion getPipelineVersionById(UUID pipelineVersionId) {
        return pipelineVersionRepository.findById(pipelineVersionId)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_VERSION_NOT_FOUND));
    }

    /**
     * Save a new version of a pipeline with the provided configuration and script.
     *
     * @param pipeline the parent Pipeline entity
     * @param script   the associated Script entity
     * @param config   the generated Jenkins job configuration (XML string)
     * @param dto      DTO containing pipeline creation request details (name, description, trigger, schedule, notifications)
     * @param name     the display name of this version (e.g., "Initial Version")
     * @return UUID of the newly saved PipelineVersion
     */
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

    /**
     * Update an existing pipeline version with new configuration, script, and metadata.
     *
     * @param pipelineVersion the PipelineVersion entity to update
     * @param dto             DTO containing updated job details (description, trigger, schedule, notifications)
     * @param script          the updated Script entity
     * @param config          the new Jenkins job configuration (XML string)
     */
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

    /**
     * Set the build state of a pipeline (success or failure) and publish an event.
     *
     * @param dto DTO containing jobId and build status flag
     */
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

    /**
     * Set the build state of a pipeline to PENDING (running).
     *
     * @param pipeline Pipeline entity
     */
    @Transactional
    public void setStatusPending(Pipeline pipeline) {
        pipeline.setLatestBuildTime(LocalDateTime.now());
        pipeline.setBuildState(Pipeline.BuildState.BUILD_RUNNING);
        pipelineRepository.save(pipeline);
    }

    /**
     * Retrieve lightweight list of pipeline versions.
     *
     * @param pipelineId Pipeline ID
     * @return List of pipeline version DTOs
     */
    public List<ResponseDto.PipelineVersionDto> getLightVersionDtoList(UUID pipelineId) {
        Pipeline pipeline = getPipelineById(pipelineId);
        return ResponseDto.toListOfPipelineVersionDtos(pipeline.getVersionList());
    }

    /**
     * Rename a pipeline version.
     *
     * @param dto DTO containing pipelineVersionId and new name
     */
    @Transactional
    public void renameVersion(RequestDto.RenameDto dto) {
        PipelineVersion pipelineVersion = getPipelineVersionById(dto.getPipelineId());
        pipelineVersion.setName(dto.getNewName());
        pipelineVersionRepository.save(pipelineVersion);
    }

    /**
     * Set the build state of a pipeline.
     *
     * @param pipeline Pipeline entity
     * @param state    Build state string (SUCCESS, FAILURE, etc.)
     */
    @Transactional
    public void setState(Pipeline pipeline, String state) {
        pipeline.setBuildState(Pipeline.BuildState.valueOf("BUILD_" + state));
        pipelineRepository.save(pipeline);
    }
}

