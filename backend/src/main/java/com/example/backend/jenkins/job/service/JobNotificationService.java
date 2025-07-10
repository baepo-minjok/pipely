package com.example.backend.jenkins.job.service;

import com.example.backend.auth.user.service.CustomUserDetails;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.repository.JenkinsInfoRepository;
import com.example.backend.jenkins.job.model.FreeStyle;
import com.example.backend.jenkins.job.model.JobNotification;
import com.example.backend.jenkins.job.model.dto.JobNotificationRequestDto;
import com.example.backend.jenkins.job.model.dto.JobNotificationResponseDto;
import com.example.backend.jenkins.job.model.pipeline.Pipeline;
import com.example.backend.jenkins.job.repository.FreeStyleRepository;
import com.example.backend.jenkins.job.repository.JobNotificationRepository;
import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobNotificationService {

    private final PipelineRepository pipelineRepository;
    private final JobNotificationRepository notificationRepository;
    private final JenkinsClientFactory jenkinsClientFactory;
    private final JenkinsInfoRepository jenkinsInfoRepository;
    private final MustacheFactory mf;

    @Transactional
    public JobNotification createJobNotification(JobNotificationRequestDto.createCredential dto, UUID userId) {
        Pipeline pipeline = pipelineRepository.findById(dto.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("Jenkins Pipeline 정보를 찾을 수 없습니다."));

        // UUID 생성 후 앞 8자리 추출
        String uuidSuffix = UUID.randomUUID().toString().substring(0, 8);

        // Credential 이름 생성
        String credentialName = String.format("%s_%s_%s_%s",
                dto.getChannel().toUpperCase(),
                pipeline.getJobName(),
                dto.getEventType().toUpperCase(),
                uuidSuffix
        );

        JobNotification notification = dto.toEntity(pipeline, credentialName);
        JobNotification saved = notificationRepository.save(notification);

        if (Boolean.TRUE.equals(dto.getShouldNotify())) {
            JenkinsClientFactory.JenkinsClient client = jenkinsClientFactory.createClientForUser(userId);
            client.createGlobalCredential(credentialName, dto.getWebhookUrl()).block();
        }

        return saved;
    }

    public List<JobNotificationResponseDto.JobNotificationListResponseDto> getUserJobNotifications(UUID userId, UUID jobId) {
        List<JobNotification> notifications = notificationRepository
                .findByPipeline_JenkinsInfo_User_IdAndPipeline_Id(userId, jobId);

        return notifications.stream()
                .map(JobNotificationResponseDto.JobNotificationListResponseDto::fromEntity)
                .toList();
    }

    public JobNotificationResponseDto.JobNotificationDetailResponseDto getNotificationDetail(String credentialName) {
        JobNotification notification = notificationRepository.findByCredentialName(credentialName)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_NOTIFICATION_NOT_FOUND));

        return JobNotificationResponseDto.JobNotificationDetailResponseDto.fromEntity(notification);
    }

    @Transactional
    public void createNotifyScript(UUID userId, UUID jobId) {
        JenkinsInfo jenkinsInfo = jenkinsInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));

        Pipeline pipeline = pipelineRepository.findByJenkinsInfoIdAndId(jenkinsInfo.getId(), jobId)
                .orElseThrow(() -> new IllegalArgumentException("Jenkins Pipeline 정보를 찾을 수 없습니다."));

        String jobName = pipeline.getJobName();

        List<JobNotification> notifications = notificationRepository
                .findByIdAndShouldNotify(jobId, true);

        if (notifications.isEmpty()) {
            log.info("[JobNotificationService] 전송할 알림이 없습니다.");
            return;
        }

        String script = createNotificationScript(jobName, notifications);

//        JenkinsClientFactory.JenkinsClient client = jenkinsClientFactory.createClientForUser(userId);
//        client.runNotificationScript(script).block();
    }

    private String createNotificationScript(String jobName, List<JobNotification> notifications) {
        Mustache mustache = mf.compile("template/notificationScript.mustache");

        List<Map<String, String>> notificationList = notifications.stream().map(n -> {
            Map<String, String> entry = new HashMap<>();
            entry.put("eventType", n.getEventType());
            entry.put("credentialId", n.getCredentialName());
            entry.put("webhookUrl", n.getWebhookUrl());
            return entry;
        }).toList();

        Map<String, Object> context = new HashMap<>();
        context.put("jobName", jobName);
        context.put("notifications", notificationList);

        StringWriter writer = new StringWriter();
        mustache.execute(writer, context);
        return writer.toString();
    }
}