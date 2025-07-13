package com.example.backend.jenkins.notification.repository;

import com.example.backend.jenkins.notification.model.JobNotification;
import com.example.backend.jenkins.notification.model.JobNotificationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobNotificationRepository extends JpaRepository<JobNotification, JobNotificationId> {
    List<JobNotification> findByIdAndShouldNotify(UUID jobId, boolean b);

    List<JobNotification> findByPipeline_JenkinsInfo_User_IdAndPipeline_Id(UUID userId, UUID jobId);

    Optional<JobNotification> findByCredentialName(String credentialName);
}
