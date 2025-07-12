package com.example.backend.jenkins.job.service;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.Stage;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.util.ScriptEditUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PipelineService {

    private final ApplicationEventPublisher publisher;
    private final JenkinsInfoService jenkinsInfoService;
    private final ConfigService configService;
    private final ScriptService scriptService;
    private final ScriptEditUtil scriptEditUtil;
    private final PipelineRepository pipelineRepository;

    @Transactional
    public void createJob(RequestDto.CreateDto requestDto) {
        // jenkins info 확인
        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(requestDto.getInfoId());

        // jenkins info에 같은 name이 존재하는지 검증
        if (pipelineRepository.findByJenkinsInfoIdAndName(requestDto.getInfoId(), requestDto.getName()).isPresent()) {
            throw new CustomException(ErrorCode.JENKINS_JOB_EXIST);
        }
        Script script = requestDto.getScriptId() != null ? scriptService.getScriptById(requestDto.getScriptId()) : null;
        // 2) 스테이지 이름 추출
        List<String> stageNames = script != null
                ? scriptEditUtil.extractStageNames(script.getScript())
                : Collections.emptyList();

        // jenkins에 보낼 config 만들기
        String config = configService.createConfig(
                configService.buildConfigContext(requestDto, script));

        // job 저장
        Pipeline pipeline = RequestDto.toEntity(requestDto, info, script, config);
        Pipeline saved = pipelineRepository.save(pipeline);
        for (int i = 0; i < stageNames.size(); i++) {
            Stage stage = Stage.builder()
                    .orderIndex(i)
                    .name(stageNames.get(i))
                    .pipeline(saved)
                    .build();
            saved.getStageList().add(stage);
        }
        info.getPipelineList().add(saved);

        // jenkins에 http 요청
        String jenkinsUrl = info.getUri() + "/createItem?name=" + requestDto.getName();
        publisher.publishEvent(new JobEvent.JobCreatedEvent(saved.getId(), saved.getConfig(), jenkinsUrl));
    }

    @Transactional
    public void updateJob(RequestDto.UpdateDto requestDto) {

        // job 검색 & info 가져오기stage 추출
        Pipeline pipeline = getPipelineById(requestDto.getPipelineId());
        JenkinsInfo info = pipeline.getJenkinsInfo();

        // script 정보 가져오기 & stage 추출
        Script script = requestDto.getScriptId() != null ? scriptService.getScriptById(requestDto.getScriptId()) : null;
        List<String> stageNames = script != null
                ? scriptEditUtil.extractStageNames(script.getScript())
                : Collections.emptyList();

        // jenkins info에 같은 name이 존재하는지 검증
        Optional<Pipeline> pipelineOptional = pipelineRepository.findByJenkinsInfoIdAndName(info.getId(), requestDto.getName());
        if (pipelineOptional.isPresent()) {
            Pipeline existing = pipelineOptional.get();
            if (!existing.equals(pipeline)) {
                throw new CustomException(ErrorCode.JENKINS_JOB_EXIST);
            }
        }

        // jenkins에 보낼 config 만들기
        RequestDto.CreateDto createDto = RequestDto.toCreateDto(requestDto);
        String config = configService.createConfig(configService.buildConfigContext(createDto, script));

        // 수정된 job 저장
        Pipeline updated = pipelineRepository.save(Pipeline.updatePipeline(pipeline, requestDto, config));
        updated.getStageList().clear();
        for (int i = 0; i < stageNames.size(); i++) {
            Stage stage = Stage.builder()
                    .orderIndex(i)
                    .name(stageNames.get(i))
                    .pipeline(updated)
                    .build();
            updated.getStageList().add(stage);
        }

        // jenkins에 http 요청
        String jenkinsUrl = info.getUri() + "/job/" + requestDto.getName() + "/config.xml";
        publisher.publishEvent(new JobEvent.JobUpdatedEvent(updated.getId(), updated.getConfig(), jenkinsUrl));
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
