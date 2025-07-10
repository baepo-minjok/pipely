package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.dto.pipeline.PipelineRequestDto.CreatePipelineDto;
import com.example.backend.jenkins.job.model.pipeline.Pipeline;
import com.example.backend.jenkins.job.model.pipeline.PipelineHistory;
import com.example.backend.jenkins.job.repository.PipelineHistoryRepository;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.service.HttpClientService;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PipelineJobService {
    private final JenkinsInfoService jenkinsInfoService;
    private final HttpClientService httpClientService;
    private final MustacheFactory mf;
    private final PipelineRepository scriptRepository;
    private final PipelineHistoryRepository scriptHistoryRepository;

    @Transactional
    public void createPipelineScriptJob(CreatePipelineDto dto) {

        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(dto.getInfoId());

        String jenkinsUrl = info.getUri() + "/createItem?name=" + dto.getJobName();

        String config = createPipelineScriptConfig(dto);

        HttpEntity<String> requestEntity = new HttpEntity<>(config, httpClientService.buildHeaders(info, new MediaType("application", "xml", StandardCharsets.UTF_8)));

        httpClientService.exchange(jenkinsUrl, HttpMethod.POST, requestEntity, String.class);

        registerPipelineScriptJob(info, dto, config);

    }

    private String createPipelineScriptConfig(CreatePipelineDto dto) {

        Mustache mustache = null;

        try {
            mustache = mf.compile("template/pipelineScriptConfig.mustache");
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MUSTACHE_FILE_NOT_FOUND);
        }
        Map<String, Object> context = new HashMap<>();
        context.put("description", dto.getDescription());
        context.put("projectUrl", dto.getProjectUrl());
        context.put("projectDisplayName", dto.getProjectDisplayName());
        context.put("githubTrigger", dto.getGithubTrigger());
        context.put("script", dto.getScript());

        StringWriter writer = new StringWriter();
        try {
            mustache.execute(writer, context).flush();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MUSTACHE_EXECUTE_FAILED);
        }

        return writer.toString();
    }

    private void registerPipelineScriptJob(JenkinsInfo info, CreatePipelineDto dto, String config) {

        Pipeline job = Pipeline.builder()
                .jenkinsInfo(info)
                .jobName(dto.getJobName())
                .description(dto.getDescription())
                .projectUrl(dto.getProjectUrl())
                .projectDisplayName(dto.getProjectDisplayName())
                .githubTrigger(dto.getGithubTrigger())
                .script(dto.getScript())

                .build();
        scriptRepository.save(job);
        info.getPipelineList().add(job);

        Integer maxVersion = scriptHistoryRepository.findMaxVersionByPipelineScript(job);
        int nextVersion = (maxVersion == null ? 1 : maxVersion + 1);

        PipelineHistory history = PipelineHistory.toHistory(job, nextVersion, config);
    }

    public Pipeline getPipelineById(UUID id) {


        return scriptRepository.findPipelineById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_NOT_FOUND));


    }

    public JenkinsInfo getJenkinsInfoByFreeStyleId(UUID id) {

        Pipeline pipeline = getPipelineById(id);
        return pipeline.getJenkinsInfo();

    }


}
