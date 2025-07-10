package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.jenkins.job.repository.ScriptRepository;
import com.example.backend.service.HttpClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PipelineService {

    private final HttpClientService httpClientService;
    private final JenkinsInfoService jenkinsInfoService;
    private final ConfigService configService;
    private final PipelineRepository pipelineRepository;
    private final ScriptRepository scriptRepository;

    @Transactional
    public void createJob(RequestDto.CreateDto requestDto) {
        Script script = getScriptById(requestDto.getScriptId());
        // jenkins info 확인
        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(requestDto.getInfoId());

        // 새로운 job 생성 요청
        String jenkinsUrl = info.getUri() + "/createItem?name=" + requestDto.getName();
        String config = configService.createConfig(configService.buildConfigContext(requestDto, script));
        HttpEntity<String> requestEntity = new HttpEntity<>(config, httpClientService.buildHeaders(info, new MediaType("application", "xml", StandardCharsets.UTF_8)));
        httpClientService.exchange(jenkinsUrl, HttpMethod.POST, requestEntity, String.class);

        // job 저장
        Pipeline pipeline = RequestDto.toEntity(requestDto, info, script, config);
        pipelineRepository.save(pipeline);

        info.getPipelineList().add(pipeline);
    }

    @Transactional
    public void updateJob(RequestDto.UpdateDto requestDto) {

        // job 검색 & info 가져오기
        Pipeline pipeline = getPipelineById(requestDto.getPipelineId());
        JenkinsInfo info = pipeline.getJenkinsInfo();
        Script script = pipeline.getScript();

        // job 수정 요청
        String jenkinsUrl = info.getUri() + "/job/" + requestDto.getName() + "/config.xml";
        RequestDto.CreateDto createDto = RequestDto.toCreateDto(requestDto);
        String config = configService.createConfig(configService.buildConfigContext(createDto, script));
        HttpEntity<String> requestEntity = new HttpEntity<>(config, httpClientService.buildHeaders(info, new MediaType("application", "xml", StandardCharsets.UTF_8)));
        httpClientService.exchange(jenkinsUrl, HttpMethod.POST, requestEntity, String.class);

        // 수정된 job 저장
        Pipeline newPipeline = Pipeline.updatePipeline(pipeline, requestDto, config);
        pipelineRepository.save(newPipeline);
    }

    public Pipeline getPipelineById(UUID id) {
        return pipelineRepository.findPipelineById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_NOT_FOUND));
    }

    @Transactional
    public void softDeletePipelineById(UUID id) {
        Pipeline pipeline = getPipelineById(id);

        pipeline.setIsDeleted(true);
        pipeline.setDeletedAt(LocalDateTime.now());

        pipelineRepository.save(pipeline);
    }

    @Transactional
    public void hardDeletePipelineById(UUID id) {
        Pipeline pipeline = getPipelineById(id);

        pipelineRepository.delete(pipeline);
    }

    public List<ResponseDto.LightJobDto> getLightJobs(UUID jenkinsInfoId) {
        return pipelineRepository.findAllByJenkinsInfoIdAndIsDeletedFalse(jenkinsInfoId)
                .stream().map(ResponseDto::entityToLightJobDto).toList();
    }

    public List<ResponseDto.LightJobDto> getDeletedLightJobs(UUID jenkinsInfoId) {
        return pipelineRepository.findAllByJenkinsInfoIdAndIsDeletedTrue(jenkinsInfoId)
                .stream().map(ResponseDto::entityToLightJobDto).toList();
    }

    public ResponseDto.DetailJobDto getDetailJob(UUID jobId) {

        return ResponseDto.entityToDetailJobDto(getPipelineById(jobId));
    }

    public Script getScriptById(UUID scriptId) {
        Optional<Script> optionalScript = scriptRepository.findById(scriptId);
        Script script = null;
        if (optionalScript.isPresent()) {
            script = optionalScript.get();
        }
        return script;
    }

    public ResponseDto.LightScriptDto generateScript(RequestDto.ScriptBaseDto requestDto) {
        UUID scriptId = requestDto.getScriptId();
        Script newScript = null;
        if (scriptId != null) {
            if (!scriptRepository.existsById(scriptId)) {
                throw new CustomException(ErrorCode.JENKINS_SCRIPT_NOT_FOUND);
            }
            String script = configService.createScript(configService.buildScriptContext(requestDto));
            newScript = Script.toEntity(requestDto, script);
            newScript.setId(scriptId);

            newScript = scriptRepository.save(newScript);
        } else {
            String script = configService.createScript(configService.buildScriptContext(requestDto));

            newScript = scriptRepository.save(Script.toEntity(requestDto, script));
        }

        return ResponseDto.entityToLightScriptDto(newScript);
    }

}
