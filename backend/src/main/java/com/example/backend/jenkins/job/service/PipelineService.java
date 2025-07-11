package com.example.backend.jenkins.job.service;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.service.HttpClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class PipelineService {

    private final ApplicationEventPublisher publisher;
    private final HttpClientService httpClientService;
    private final JenkinsInfoService jenkinsInfoService;
    private final ConfigService configService;
    private final ScriptService scriptService;
    private final PipelineRepository pipelineRepository;

    @Transactional
    public void createJob(RequestDto.CreateDto requestDto) {

        // jenkins info에 같은 name이 존재하는지 검증
        if (pipelineRepository.findByJenkinsInfoIdAndName(requestDto.getInfoId(), requestDto.getName()).isPresent()) {
            throw new CustomException(ErrorCode.JENKINS_JOB_EXIST);
        }
        Script script = requestDto.getScriptId() != null ? scriptService.getScriptById(requestDto.getScriptId()) : null;

        // jenkins info 확인
        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(requestDto.getInfoId());


        String config = configService.createConfig(
                configService.buildConfigContext(requestDto, script));

        Pipeline pipeline = RequestDto.toEntity(requestDto,
                info,
                script,
                config);

        // job 저장
        Pipeline saved = pipelineRepository.save(pipeline);

        info.getPipelineList().add(pipeline);
        
        publisher.publishEvent(new JobCreatedEvent(saved.getId(), saved.getConfig()));
        /*// 새로운 job 생성 요청
        String jenkinsUrl = info.getUri() + "/createItem?name=" + requestDto.getName();
        HttpEntity<String> requestEntity = new HttpEntity<>(config, httpClientService.buildHeaders(info, new MediaType("application", "xml", StandardCharsets.UTF_8)));
        httpClientService.exchange(jenkinsUrl, HttpMethod.POST, requestEntity, String.class);*/
    }

    @Transactional
    public void updateJob(RequestDto.UpdateDto requestDto) {

        // job 검색 & info 가져오기
        Pipeline pipeline = getPipelineById(requestDto.getPipelineId());
        JenkinsInfo info = pipeline.getJenkinsInfo();
        Script script = pipeline.getScript();

        // jenkins info에 같은 name이 존재하는지 검증
        Optional<Pipeline> pipelineOptional = pipelineRepository.findByJenkinsInfoIdAndName(info.getId(), requestDto.getName());
        if (pipelineOptional.isPresent()) {
            Pipeline existing = pipelineOptional.get();
            if (!existing.equals(pipeline)) {
                throw new CustomException(ErrorCode.JENKINS_JOB_EXIST);
            }
        }

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
        return pipelineRepository.findWithInfoAndScriptById(id)
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
        return pipelineRepository.findActiveWithScriptByJenkinsInfoId(jenkinsInfoId)
                .stream().map(ResponseDto::entityToLightJobDto).toList();
    }

    public List<ResponseDto.LightJobDto> getDeletedLightJobs(UUID jenkinsInfoId) {
        return pipelineRepository.findDeletedWithScriptByJenkinsInfoId(jenkinsInfoId)
                .stream().map(ResponseDto::entityToLightJobDto).toList();
    }

    public ResponseDto.DetailJobDto getDetailJob(UUID jobId) {

        return ResponseDto.entityToDetailJobDto(getPipelineById(jobId));
    }

    // Pipeline 권한 확인하는 AOP
    public boolean isOwner(Users user, UUID pipelineId) {

        Pipeline pipeline = pipelineRepository.findWithInfoAndScriptAndUserById(pipelineId)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_NOT_FOUND));
        JenkinsInfo info = pipeline.getJenkinsInfo();
        UUID userId = user.getId();
        UUID confirmUserId = info.getUser().getId();
        log.info(user.getId().toString());
        log.info(info.getUser().getId().toString());
        return userId.equals(confirmUserId);
    }
}
