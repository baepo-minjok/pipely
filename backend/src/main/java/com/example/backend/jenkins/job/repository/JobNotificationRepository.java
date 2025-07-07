package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.JobNotification;
import com.example.backend.jenkins.job.model.JobNotificationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobNotificationRepository extends JpaRepository<JobNotification, JobNotificationId> {
    long countByChannelAndEventType(String channel, String eventType);

    List<JobNotification> findByIdAndShouldNotify(UUID jobId, boolean b);
}
