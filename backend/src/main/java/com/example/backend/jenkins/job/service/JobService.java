package com.example.backend.jenkins.job.service;

import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.service.HttpClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class JobService {

    private final HttpClientService httpClientService;
    private final JenkinsInfoService jenkinsInfoService;
    private final ConfigService configService;


    public void createJob(RequestDto.CreateDto requestDto) {

        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(requestDto.getInfoId());

        String jenkinsUrl = info.getUri() + "/createItem?name=" + requestDto.getName();

        String config = configService.createConfig(configService.buildConfigContext(requestDto));

        HttpEntity<String> requestEntity = new HttpEntity<>(config, httpClientService.buildHeaders(info, new MediaType("application", "xml", StandardCharsets.UTF_8)));

        httpClientService.exchange(jenkinsUrl, HttpMethod.POST, requestEntity, String.class);
    }

}
