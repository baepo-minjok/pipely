package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.JobNotification;
import com.example.backend.jenkins.job.model.JobNotificationId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobNotificationRepository extends JpaRepository<JobNotification, JobNotificationId> {
    long countByChannelAndEventType(String channel, String eventType);
}
