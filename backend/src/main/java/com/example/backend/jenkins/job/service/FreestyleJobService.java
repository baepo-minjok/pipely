package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.FreeStyle;
import com.example.backend.jenkins.job.model.dto.JobRequestDto.CreateFreestyleDto;
import com.example.backend.jenkins.job.repository.FreeStyleRepository;
import com.example.backend.service.HttpClientService;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FreestyleJobService {

    private final MustacheFactory mf;
    private final JenkinsInfoService jenkinsInfoService;
    private final HttpClientService httpClientService;
    private final FreeStyleRepository freeStyleRepository;

    @Transactional
    public void createFreestyleJob(CreateFreestyleDto dto) {

        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(dto.getInfoId());

        String jenkinsUrl = info.getUri() + "/createItem?name=" + dto.getJobName();
        String username = info.getJenkinsId();
        String apiToken = info.getSecretKey();

        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "xml", StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);

        String config = createFreestyleConfig(dto);

        HttpEntity<String> requestEntity = new HttpEntity<>(config, headers);

        httpClientService.exchange(jenkinsUrl, HttpMethod.POST, requestEntity, String.class);

        registerFreeStyleJob(info, dto);
    }

    private String createFreestyleConfig(CreateFreestyleDto dto) {

        Mustache mustache = null;

        try {
            mustache = mf.compile("template/freeStyleConfig.mustache");
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MUSTACHE_FILE_NOT_FOUND);
        }
        Map<String, Object> context = new HashMap<>();
        context.put("description", dto.getDescription());
        context.put("projectUrl", dto.getProjectUrl());
        context.put("projectDisplayName", dto.getProjectDisplayName());
        context.put("githubTrigger", dto.getGithubTrigger());
        context.put("repositoryUrl", dto.getRepositoryUrl());
        context.put("branch", dto.getBranch());
        context.put("script", dto.getScript());

        StringWriter writer = new StringWriter();
        try {
            mustache.execute(writer, context).flush();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MUSTACHE_EXECUTE_FAILED);
        }

        return writer.toString();
    }

    private void registerFreeStyleJob(JenkinsInfo info, CreateFreestyleDto dto) {

        FreeStyle job = FreeStyle.builder()
                .jenkinsInfo(info)
                .jobName(dto.getJobName())
                .description(dto.getDescription())
                .projectUrl(dto.getProjectUrl())
                .projectDisplayName(dto.getProjectDisplayName())
                .githubTrigger(dto.getGithubTrigger())
                .repositoryUrl(dto.getRepositoryUrl())
                .branch(dto.getBranch())
                .script(dto.getScript())
                .build();
        freeStyleRepository.save(job);
        info.getFreeStyleList().add(job);
    }
}
