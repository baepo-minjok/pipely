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
import java.util.*;
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
    public void createJob(RequestDto.CreateDto dto) {
        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(dto.getInfoId());
        ensureUniqueName(info.getId(), dto.getName());

        Script script = loadScript(dto.getScriptId());
        String config = buildConfig(dto, script);

        Pipeline pipeline = savePipeline(dto, info, script, config);

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
        JenkinsInfo info = pipeline.getJenkinsInfo();
        String preName = pipeline.getName();
        boolean isRenamed = isRenamed(preName, dto);

        Script script = loadScript(dto.getScriptId());

        RequestDto.CreateDto createDto = RequestDto.toCreateDto(dto, info.getId());
        String config = buildConfig(createDto, script);
        applyPipelineChanges(pipeline, createDto, script, config);

        if (isRenamed) {    // 수정할 Job 이름이 다를 때
            // 이미 존재하는 이름인지 검사
            ensureUniqueName(info.getId(), dto.getName(), pipeline);

            // 기존 job 삭제 요청
            deleteJobOnJenkins(info, preName, () -> compensationService.rollbackLatestVersion(pipeline));
            // 생성 요청
            callJenkins(info.getUri() + "/createItem?name=" + dto.getName(),
                    config, info, HttpMethod.POST,
                    () -> compensationService.rollbackLatestVersion(pipeline));
        } else {        // 수정할 Job 이름이 같을 때
            // 수정 요청
            callJenkins(info.getUri() + "/job/" + dto.getName() + "/config.xml",
                    config, info, HttpMethod.POST, () -> compensationService.rollbackLatestVersion(pipeline));
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
        Pipeline entity = RequestDto.toEntity(dto, info);
        saveVersion(entity, script, config, dto);
        Pipeline saved = pipelineRepository.save(entity);
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

    private List<String> extractStageNames(Script script) {
        if (script == null) return Collections.emptyList();
        return scriptEditUtil.extractStageNames(script.getScript())
                .stream()
                .map(s -> s.toUpperCase().replaceAll("\\W+", "_"))
                .collect(Collectors.toList());
    }

    private void applyPipelineChanges(Pipeline pipeline, RequestDto.CreateDto dto, Script script, String config) {
        pipeline.setName(dto.getName());
        pipeline.setUpdatedAt(LocalDateTime.now());

        saveVersion(pipeline, script, config, dto);

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


    /*@Transactional
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
    }*/

    public List<ResponseDto.PipelineVersionDto> getPipelineVersions(UUID pipelineId) {
        Pipeline pipeline = getPipelineById(pipelineId);
        return pipeline.getVersionList().stream()
                .map(ResponseDto::entityToPipelineVersionDto)
                .toList();
    }

    public PipelineVersion getLatestVersion(Pipeline pipeline) {
        int latestVersion = pipeline.getLatestVersion();

        return pipeline.getVersionList().get(latestVersion);
    }

    //새 버전 저장
    private void saveVersion(Pipeline pipeline, Script script, String config, RequestDto.CreateDto dto) {
        Integer newVersion = pipeline.getVersionList().stream()
                .map(PipelineVersion::getVersion)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        pipeline.setLatestVersion(newVersion);

        PipelineVersion version = PipelineVersion.builder()
                .version(newVersion)
                .description(dto.getDescription())
                .isTriggered(dto.getTrigger())
                .schedule(dto.getSchedule())
                .createdAt(LocalDateTime.now())
                .config(config)
                .isSuccessfulBuild(null)
                .script(script)
                .pipeline(pipeline)
                .build();

        createStages(version, script);

        pipeline.getVersionList().add(version);
    }

    // 특정 파이프라인버전 삭제
    // 조건: 가장 최신 버전은 삭제 할 수 없음
    @Transactional
    public void deletePipelineVersion(UUID pipelineId, Integer version) {
        Pipeline pipeline = getPipelineById(pipelineId);

        // 최신 버전은 삭제 불가
        if (pipeline.getLatestVersion() != null && pipeline.getLatestVersion().equals(version)) {
            throw new CustomException(ErrorCode.CANNOT_DELETE_LATEST_VERSION);
        }

        // 삭제할 대상 버전 찾기
        PipelineVersion toDelete = pipeline.getVersionList().stream()
                .filter(v -> v.getVersion().equals(version))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.VERSION_NOT_FOUND));

        // 파이프라인의 버전 리스트에서 해당 버전 제거 (Cascade 설정으로 DB에서도 삭제됨)
        pipeline.getVersionList().remove(toDelete);
        pipelineRepository.save(pipeline);

    }

    // 현재 최신 버전을 제거하고 이전 버전으로 롤백
    @Transactional
    public void rollbackToPreviousVersion(UUID pipelineId) {
        Pipeline pipeline = getPipelineById(pipelineId);
        int currentVersion = pipeline.getLatestVersion();

        // 버전이 1 이하인 경우 롤백 불가
        if (currentVersion <= 1) {
            throw new CustomException(ErrorCode.NO_PREVIOUS_VERSION);
        }

        // 존재하는 이전 버전들 중 가장 높은 값 찾기
        PipelineVersion target = pipeline.getVersionList().stream()
                .filter(v -> v.getVersion() < currentVersion)
                .max(Comparator.comparingInt(PipelineVersion::getVersion))
                .orElseThrow(() -> new CustomException(ErrorCode.NO_PREVIOUS_VERSION));


        // Jenkins job 수정
        rollbackPipelineToVersion(pipeline, target);

        // 롤백된 버전으로 최신 버전 변경
        pipeline.setLatestVersion(target.getVersion());
        pipelineRepository.save(pipeline);
    }

    // 특정버전으로 롤백 & latestVersion을 해당 버전으로 변경
    @Transactional
    public void rollbackToSpecificVersion(UUID pipelineId, int version) {
        Pipeline pipeline = getPipelineById(pipelineId);

        // 롤백할 대상 버전 조회
        PipelineVersion target = pipeline.getVersionList().stream()
                .filter(v -> v.getVersion().equals(version))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.VERSION_NOT_FOUND));

        // Jenkins job 수정
        rollbackPipelineToVersion(pipeline, target);

        //롤백된 버전으로 최신버전 수정
        pipeline.setLatestVersion(version);
        pipelineRepository.save(pipeline);
    }

    // Jenkins에 롤백된 config 반영해서 수정
    private void rollbackPipelineToVersion(Pipeline pipeline, PipelineVersion version) {
        pipeline.setUpdatedAt(LocalDateTime.now());

        callJenkins(
                pipeline.getJenkinsInfo().getUri() + "/job/" + pipeline.getName() + "/config.xml",
                version.getConfig(),
                pipeline.getJenkinsInfo(),
                HttpMethod.POST,
                () -> compensationService.rollbackLatestVersion(pipeline)
        );
    }
}
