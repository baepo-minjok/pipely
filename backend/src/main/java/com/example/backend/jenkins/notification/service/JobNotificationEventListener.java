package com.example.backend.jenkins.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobNotificationEventListener {

    private final RestTemplate restTemplate;

    @EventListener
    public <T> void handleJobNotificationEvent(JobNotificationEvent<T> event) {
        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    event.getJenkinsUrl(),
                    event.getMethod(),
                    event.getRequestEntity(),
                    event.getResponseType()
            );
            log.info("[Jenkins Update] Job updated successfully (pipelineId={}): status={}",
                    event.getPipelineId(), response.getStatusCode());
        } catch (Exception e) {
            log.error("[Jenkins Update] Failed to update Jenkins job (pipelineId={}): {}",
                    event.getPipelineId(), e.getMessage());
        }
    }
}