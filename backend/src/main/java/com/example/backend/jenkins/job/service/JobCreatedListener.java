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
    public void handleJobCreated(JobEvent.JobCreatedEvent evt) {
        Pipeline pipeline = pipelineRepository.findById(evt.getPipelineId())
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_NOT_FOUND));

        JenkinsInfo info = pipeline.getJenkinsInfo();

        try {
            exchange(evt.getConfig(), evt.getUrl(), info);
        } catch (Exception e) {
            compensationService.deletePipeline(evt.getPipelineId());
        }
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleJobUpdate(JobEvent.JobUpdatedEvent evt) {
        Pipeline pipeline = pipelineRepository.findById(evt.getPipelineId())
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_NOT_FOUND));

        JenkinsInfo info = pipeline.getJenkinsInfo();

        try {
            exchange(evt.getConfig(), evt.getUrl(), info);
        } catch (Exception e) {
            //TODO update 실패시 전버전으로 롤백
        }
    }

    public void exchange(String config, String url, JenkinsInfo info) {

        HttpEntity<String> req = new HttpEntity<>(
                config,
                httpClientService.buildHeaders(
                        info,
                        new MediaType("application", "xml", StandardCharsets.UTF_8)
                )
        );
        httpClientService.exchange(url, HttpMethod.POST, req, String.class);
    }

}

