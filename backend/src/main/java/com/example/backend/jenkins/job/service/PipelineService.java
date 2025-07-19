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
import com.example.backend.jenkins.job.model.dto.SnapshotRollbackDto;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.jenkins.job.repository.PipelineVersionRepository;
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
import java.util.ArrayList;
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
    private final CompensationService compensationService;
    private final StageService stageService;
    private final PipelineVersionRepository pipelineVersionRepository;

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

        callJenkins(info.getUri() + "/createItem?name=" + dto.getName(),
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

        SnapshotRollbackDto rollbackDto = SnapshotRollbackDto.builder()
                .versionId(version.getId())
                .name(version.getName())
                .isTriggered(version.getIsTriggered())
                .config(version.getConfig())
                .description(version.getDescription())
                .schedule(version.getSchedule())
                .pipelineId(pipeline.getId())
                .build();
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
            deleteJobOnJenkins(info, preName, () -> compensationService.rollbackLatestVersion(rollbackDto));
            // 생성 요청
            callJenkins(info.getUri() + "/createItem?name=" + dto.getName(),
                    config, info, HttpMethod.POST,
                    () -> compensationService.rollbackLatestVersion(rollbackDto));
        } else {        // 수정할 Job 이름이 같을 때
            // 수정 요청
            callJenkins(info.getUri() + "/job/" + dto.getName() + "/config.xml",
                    config, info, HttpMethod.POST, () -> compensationService.rollbackLatestVersion(rollbackDto));
        }
    }

    @Transactional
    public void snapshotVersion(UUID pipelineVersionId, String snapshotName) {
        //스냅샷할 버전
        PipelineVersion version = pipelineVersionRepository.findById(pipelineVersionId)
                .orElseThrow(() -> new CustomException(ErrorCode.VERSION_NOT_FOUND));

        PipelineVersion snapshot = PipelineVersion.builder()
                .name(snapshotName)
                .description(version.getDescription())
                .isTriggered(version.getIsTriggered())
                .schedule(version.getSchedule())
                .createdAt(LocalDateTime.now())
                .config(version.getConfig())
                .script(version.getScript())
                .stageList(new ArrayList<>(version.getStageList()))
                .pipeline(version.getPipeline())
                .build();

        version.getPipeline().getVersionList().add(snapshot);

        pipelineVersionRepository.save(snapshot);
        pipelineVersionRepository.flush();
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

    private void createStages(PipelineVersion pipelineVersion, Script script) {
        List<String> names = extractStageNames(script);
        for (int i = 0; i < names.size(); i++) {
            Stage stage = Stage.builder()
                    .orderIndex(i)
                    .name(names.get(i))
                    .pipelineVersion(pipelineVersion)
                    .build();
            pipelineVersion.getStageList().add(stage);
        }
    }

    public void updateStages(PipelineVersion pipelineVersion, Script script) {
        stageService.deleteByPipelineVersionId(pipelineVersion.getId());
        createStages(pipelineVersion, script);
    }

    private List<String> extractStageNames(Script script) {
        if (script == null) return Collections.emptyList();
        return scriptEditUtil.extractStageNames(script.getScript())
                .stream()
                .map(s -> s.toUpperCase().replaceAll("\\W+", "_"))
                .collect(Collectors.toList());
    }

    private void applyPipelineChanges(Pipeline pipeline, RequestDto.UpdateDto dto, Script script, String config) {
        pipeline.setName(dto.getName());
        pipeline.setUpdatedAt(LocalDateTime.now());

        updateVersion(getLatestVersion(pipeline), dto, script, config);

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
        PipelineVersion pipelineVersion = getLatestVersion(pipeline);
        JenkinsInfo info = pipeline.getJenkinsInfo();
        LocalDateTime time = pipeline.getDeletedAt();

        ensureUniqueName(info.getId(), pipeline.getName(), pipeline);

        compensationService.softDeletePipeline(pipeline, null, false);

        String config = pipelineVersion.getConfig();

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

    public PipelineVersion getLatestVersion(Pipeline pipeline) {
        return pipelineVersionRepository.findWithScriptAndStageListById(pipeline.getLatestVersionId())
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_VERSION_NOT_FOUND));
    }

    public PipelineVersion getPipelineVersionById(UUID pipelineVersionId) {
        return pipelineVersionRepository.findById(pipelineVersionId)
                .orElseThrow(() -> new CustomException(ErrorCode.VERSION_NOT_FOUND));
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

        createStages(version, script);

        pipeline.getVersionList().add(version);

        PipelineVersion savedVersion = pipelineVersionRepository.save(version);
        pipelineVersionRepository.flush();
        return savedVersion.getId();
    }

    private void updateVersion(PipelineVersion pipelineVersion, RequestDto.UpdateDto dto, Script script, String config) {

        pipelineVersion.setDescription(dto.getDescription());
        pipelineVersion.setIsTriggered(dto.getTrigger());
        pipelineVersion.setSchedule(dto.getSchedule());
        pipelineVersion.setConfig(config);
        pipelineVersion.setScript(script);

        updateStages(pipelineVersion, script);

        pipelineVersionRepository.save(pipelineVersion);
        pipelineVersionRepository.flush();
    }

    // 특정 파이프라인버전 삭제
    // 조건: 가장 최신 버전은 삭제 할 수 없음
    @Transactional
    public void deletePipelineVersion(UUID pipelineId, UUID pipelineVersionId) {
        Pipeline pipeline = getPipelineById(pipelineId);

        // 최신 버전은 삭제 불가
        if (pipeline.getLatestVersionId() != null && pipeline.getLatestVersionId().equals(pipelineVersionId)) {
            throw new CustomException(ErrorCode.CANNOT_DELETE_LATEST_VERSION);
        }

        // 삭제할 대상 버전 찾기
        PipelineVersion toDelete = pipeline.getVersionList().stream()
                .filter(v -> v.getId().equals(pipelineVersionId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.VERSION_NOT_FOUND));

        // 파이프라인의 버전 리스트에서 해당 버전 제거 (Cascade 설정으로 DB에서도 삭제됨)
        pipeline.getVersionList().remove(toDelete);
        pipelineRepository.save(pipeline);

    }

    @Transactional
    public void rollbackToSnapshot(UUID pipelineId, UUID snapshotVersionId) {
        Pipeline pipeline = getPipelineById(pipelineId);
        PipelineVersion version = getPipelineVersionById(snapshotVersionId);

        UUID previousVersionId = pipeline.getLatestVersionId();

        JenkinsInfo info = pipeline.getJenkinsInfo();
        String config = version.getConfig();

        //snapshot 버전으로 변경
        pipeline.setLatestVersionId(snapshotVersionId);
        pipelineRepository.save(pipeline);

        callJenkins(info.getUri() + "/job/" + pipeline.getName() + "/config.xml",
                config, info, HttpMethod.POST, () -> compensationService.rollbackPipelineLatestVersion(pipeline, previousVersionId));
    }

}

