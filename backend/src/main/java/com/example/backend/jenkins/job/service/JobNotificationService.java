package com.example.backend.jenkins.job.service;

import com.example.backend.auth.user.service.CustomUserDetails;
import com.example.backend.jenkins.job.model.FreeStyle;
import com.example.backend.jenkins.job.model.JobNotification;
import com.example.backend.jenkins.job.model.dto.JobNotificationRequestDto;
import com.example.backend.jenkins.job.repository.FreeStyleRepository;
import com.example.backend.jenkins.job.repository.JobNotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobNotificationService {

    private final FreeStyleRepository freeStyleRepository;
    private final JobNotificationRepository notificationRepository;
    private final JenkinsClientFactory jenkinsClientFactory;

    @Transactional
    public JobNotification createJobNotification(JobNotificationRequestDto dto, UUID userId) {
        UUID currentUserId = getCurrentUserId();

        FreeStyle freeStyle = freeStyleRepository.findById(dto.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("FreeStyleJob not found"));

        long count = notificationRepository.countByChannelAndEventType(dto.getChannel(), dto.getEventType());
        String credentialName = String.format("%s_%s_%s_%03d",
                dto.getChannel().toUpperCase(),
                freeStyle.getJobName(),
                dto.getEventType().toUpperCase(),
                count + 1);

        JobNotification notification = dto.toEntity(freeStyle, credentialName);
        JobNotification saved = notificationRepository.save(notification);

        if (Boolean.TRUE.equals(dto.getShouldNotify())) {
            JenkinsClientFactory.JenkinsClient client = jenkinsClientFactory.createClientForUser(userId);
            client.createGlobalCredential(credentialName, dto.getWebhookUrl()).block();
        }

        return saved;
    }

    private UUID getCurrentUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return userDetails.getUser().getId();
    }
}