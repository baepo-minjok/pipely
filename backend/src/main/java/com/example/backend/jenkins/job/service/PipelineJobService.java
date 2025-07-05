package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.dto.pipeline.PipelineScriptRequestDto.CreatePipelineScriptDto;
import com.example.backend.jenkins.job.model.pipeline.PipelineScript;
import com.example.backend.jenkins.job.model.pipeline.PipelineScriptHistory;
import com.example.backend.jenkins.job.repository.PipelineScriptHistoryRepository;
import com.example.backend.jenkins.job.repository.PipelineScriptRepository;
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

@RequiredArgsConstructor
@Service
public class PipelineJobService {
    private final JenkinsInfoService jenkinsInfoService;
    private final HttpClientService httpClientService;
    private final MustacheFactory mf;
    private final PipelineScriptRepository scriptRepository;
    private final PipelineScriptHistoryRepository scriptHistoryRepository;

    @Transactional
    public void createPipelineScriptJob(CreatePipelineScriptDto dto) {

        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(dto.getInfoId());

        String jenkinsUrl = info.getUri() + "/createItem?name=" + dto.getJobName();

        String config = createPipelineScriptConfig(dto);

        HttpEntity<String> requestEntity = new HttpEntity<>(config, httpClientService.buildHeaders(info, new MediaType("application", "xml", StandardCharsets.UTF_8)));

        httpClientService.exchange(jenkinsUrl, HttpMethod.POST, requestEntity, String.class);

        registerPipelineScriptJob(info, dto, config);

    }

    private String createPipelineScriptConfig(CreatePipelineScriptDto dto) {

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

    private void registerPipelineScriptJob(JenkinsInfo info, CreatePipelineScriptDto dto, String config) {

        PipelineScript job = PipelineScript.builder()
                .jenkinsInfo(info)
                .jobName(dto.getJobName())
                .description(dto.getDescription())
                .projectUrl(dto.getProjectUrl())
                .projectDisplayName(dto.getProjectDisplayName())
                .githubTrigger(dto.getGithubTrigger())
                .script(dto.getScript())

                .build();
        scriptRepository.save(job);
        info.getPipelineScriptList().add(job);

        Integer maxVersion = scriptHistoryRepository.findMaxVersionByPipelineScript(job);
        int nextVersion = (maxVersion == null ? 1 : maxVersion + 1);

        PipelineScriptHistory history = PipelineScriptHistory.toHistory(job, nextVersion, config);
    }
}
