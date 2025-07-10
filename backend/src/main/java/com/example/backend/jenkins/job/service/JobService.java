package com.example.backend.jenkins.job.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.Job;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.repository.JobRepository;
import com.example.backend.service.HttpClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobService {

    private final HttpClientService httpClientService;
    private final JenkinsInfoService jenkinsInfoService;
    private final JobRepository jobRepository;
    private final ConfigService configService;


    public void createJob(RequestDto.CreateDto requestDto) {

        // jenkins info 확인
        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(requestDto.getInfoId());

        // 새로운 job 생성 요청
        String jenkinsUrl = info.getUri() + "/createItem?name=" + requestDto.getName();
        String config = configService.createConfig(configService.buildConfigContext(requestDto));
        HttpEntity<String> requestEntity = new HttpEntity<>(config, httpClientService.buildHeaders(info, new MediaType("application", "xml", StandardCharsets.UTF_8)));
        httpClientService.exchange(jenkinsUrl, HttpMethod.POST, requestEntity, String.class);

        // job 저장
        Job job = RequestDto.toEntity(requestDto, info);
        jobRepository.save(job);
        info.getJobList().add(job);
    }

    public String generateScript(RequestDto.GenerateScriptDto requestDto) {

        return configService.createScript(configService.buildScriptContext(requestDto));
    }

    public void updateJob(RequestDto.UpdateDto requestDto) {

        // job 검색 & info 가져오기
        Job job = getJobById(requestDto.getJobId());
        JenkinsInfo info = job.getJenkinsInfo();

        String jenkinsUrl = info.getUri() + "/job/" + requestDto.getName() + "/config.xml";
        RequestDto.CreateDto createDto = RequestDto.toCreateDto(requestDto);
        String config = configService.createConfig(configService.buildConfigContext(createDto));
        HttpEntity<String> requestEntity = new HttpEntity<>(config, httpClientService.buildHeaders(info, new MediaType("application", "xml", StandardCharsets.UTF_8)));
        httpClientService.exchange(jenkinsUrl, HttpMethod.POST, requestEntity, String.class);

        // 수정된 job 저장
        Job newJob = RequestDto.toExistEntity(createDto, info, requestDto.getJobId());
        jobRepository.save(newJob);
    }

    public Job getJobById(UUID id) {
        return jobRepository.findJenkinsInfoById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_NOT_FOUND));
    }
}
