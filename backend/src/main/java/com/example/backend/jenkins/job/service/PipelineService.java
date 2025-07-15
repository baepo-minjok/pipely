package com.example.backend.jenkins.job.service;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.PipelineVersion;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.Stage;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.service.HttpClientService;
import com.example.backend.util.ScriptEditUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
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
    private final ScriptEditUtil scriptEditUtil;
    private final PipelineRepository pipelineRepository;
    private final StageService stageService;
    private final CompensationService compensationService;

    /**
     * Create a new Jenkins job and persist the pipeline.
     */
    @Transactional
    public UUID createJob(RequestDto.CreateDto dto) {
        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(dto.getInfoId());
        ensureUniqueName(info.getId(), dto.getName());

        Script script = loadScript(dto.getScriptId());
        String config = buildConfig(dto, script);

        Pipeline pipeline = savePipeline(dto, info, script, config);
        createStages(pipeline, script);

        callJenkins(info.getUri() + "/createItem?name=" + dto.getName(),
                config, info, HttpMethod.POST,
                () -> compensationService.deletePipeline(pipeline.getId()));

        return pipeline.getId();
    }

    /**
     * Update an existing Jenkins job or recreate if renamed.
     */
    @Transactional
    public void updateJob(RequestDto.UpdateDto dto) {
        Pipeline pipeline = getPipelineById(dto.getPipelineId());
        JenkinsInfo info = pipeline.getJenkinsInfo();
        String preName = pipeline.getName();
        boolean isRenamed = isRenamed(preName, dto);

        Script script = loadScript(dto.getScriptId());
        updateStages(pipeline, script);
        ensureUniqueName(info.getId(), dto.getName(), pipeline);

        String config = buildConfig(RequestDto.toCreateDto(dto, info.getId()), script);
        applyPipelineChanges(pipeline, dto, config);
        if (isRenamed) {
            callJenkins(info.getUri() + "/createItem?name=" + dto.getName(),
                    config, info, HttpMethod.POST,
                    () -> compensationService.rollback());
            deleteJobOnJenkins(info, preName, () -> compensationService.rollback());
        } else {
            callJenkins(info.getUri() + "/job/" + dto.getName() + "/config.xml",
                    config, info, HttpMethod.POST, () -> compensationService.rollback());
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
        deleteJobOnJenkins(pipeline.getJenkinsInfo(), pipeline.getName(), () -> compensationService.softDeletePipeline(pipeline, null, false));
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
        return ResponseDto.entityToDetailJobDto(getPipelineById(jobId));
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

    private String buildConfig(RequestDto.CreateDto dto, Script script) {
        return configService.createConfig(
                configService.buildConfigContext(dto, script)
        );
    }

    private Pipeline savePipeline(RequestDto.CreateDto dto, JenkinsInfo info, Script script, String config) {
        Pipeline entity = RequestDto.toEntity(dto, info, script, config);
        return pipelineRepository.save(entity);
    }

    private void createStages(Pipeline pipeline, Script script) {
        List<String> names = extractStageNames(script);
        for (int i = 0; i < names.size(); i++) {
            Stage stage = Stage.builder()
                    .orderIndex(i)
                    .name(names.get(i))
                    .pipeline(pipeline)
                    .build();
            pipeline.getStageList().add(stage);
        }
        pipelineRepository.flush();
    }

    private void updateStages(Pipeline pipeline, Script script) {
        stageService.deleteByPipelineId(pipeline.getId());
        pipeline.getStageList().clear();
        createStages(pipeline, script);
    }

    private List<String> extractStageNames(Script script) {
        if (script == null) return Collections.emptyList();
        return scriptEditUtil.extractStageNames(script.getScript())
                .stream()
                .map(s -> s.toUpperCase().replaceAll("\\W+", "_"))
                .collect(Collectors.toList());
    }

    private void applyPipelineChanges(Pipeline pipeline, RequestDto.UpdateDto dto, String config) {
        pipeline.setName(dto.getName());
        pipeline.setDescription(dto.getDescription());
        pipeline.setIsTriggered(dto.getTrigger());
        pipeline.setConfig(config);
        pipeline.setUpdatedAt(LocalDateTime.now());
        pipeline.setSchedule(dto.getSchedule());
        pipelineRepository.save(pipeline);
        pipelineRepository.flush();
    }

    private void callJenkins(String url, String body, JenkinsInfo info, HttpMethod method, Runnable onError) {
        HttpEntity<String> req = new HttpEntity<>(
                body,
                httpClientService.buildHeaders(
                        info,
                        new MediaType("application", "xml", StandardCharsets.UTF_8)
                )
        );
        try {
            httpClientService.exchange(url, method, req, String.class);
        } catch (Exception e) {
            if (onError != null) onError.run();
            throw e;
        }
    }

    private void deleteJobOnJenkins(JenkinsInfo info, String name, Runnable onError) {
        String url = info.getUri() + "/job/" + name + "/doDelete";
        HttpEntity<String> req = new HttpEntity<>(
                httpClientService.buildHeaders(info, MediaType.APPLICATION_FORM_URLENCODED)
        );
        try {
            httpClientService.exchange(url, HttpMethod.POST, req, String.class);
        } catch (CustomException e) {
            if (!ErrorCode.INVALID_ENDPOINT.equals(e.getErrorCode())) {
                if (onError != null) onError.run();
                throw e;
            }
        }
    }


    @Transactional
    public void restorationJob(UUID pipelineId) {
        Pipeline pipeline = getPipelineById(pipelineId);
        JenkinsInfo info = pipeline.getJenkinsInfo();
        LocalDateTime time = pipeline.getDeletedAt();

        ensureUniqueName(info.getId(), pipeline.getName(), pipeline);

        compensationService.softDeletePipeline(pipeline, null, false);

        String config = pipeline.getConfig();

        callJenkins(info.getUri() + "/createItem?name=" + pipeline.getName(),
                config, info, HttpMethod.POST,
                () -> compensationService.softDeletePipeline(pipeline, time, true));
    }

    public List<ResponseDto.PipelineVersionDto> getPipelineVersions(UUID pipelineId) {
        Pipeline pipeline = getPipelineById(pipelineId);
        return pipeline.getVersionList().stream()
                .map(ResponseDto::entityToPipelineVersionDto)
                .toList();
    }

    //새 버전 저장
    private void saveVersion(Pipeline pipeline, Script script, String config) {
        Integer newVersion = Optional.ofNullable(pipeline.getLatestVersion()).orElse(0) + 1;
        pipeline.setLatestVersion(newVersion);

        PipelineVersion version = PipelineVersion.builder()
                .pipeline(pipeline)
                .version(newVersion)
                .createdAt(LocalDateTime.now())
                .script(script)
                .config(config)
                .isSuccessfulBuild(null)
                .build();

        pipeline.getVersionList().add(version);
    }


}
