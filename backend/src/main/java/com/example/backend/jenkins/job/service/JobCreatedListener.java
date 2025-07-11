package com.example.backend.jenkins.job.service;


import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.service.HttpClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobCreatedListener {

    private final HttpClientService httpClientService;
    private final PipelineRepository pipelineRepository;
    private final CompensationService compensationService;

    /**
     * AFTER_COMMIT 단계에서 호출: Jenkins API 요청, 실패 시 보상(compensating) 삭제.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleJobCreated(JobCreatedEvent evt) {
        Pipeline pipeline = pipelineRepository.findById(evt.getPipelineId())
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_NOT_FOUND));

        JenkinsInfo info = pipeline.getJenkinsInfo();
        String jenkinsUrl = info.getUri()
                + "/createItem?name=" + pipeline.getName();

        HttpEntity<String> req = new HttpEntity<>(
                evt.getConfig(),
                httpClientService.buildHeaders(
                        info,
                        new MediaType("application", "xml", StandardCharsets.UTF_8)
                )
        );
        try {
            httpClientService.exchange(jenkinsUrl, HttpMethod.POST, req, String.class);
        } catch (Exception e) {
            compensationService.deletePipeline(evt.getPipelineId());
        }
    }

}

