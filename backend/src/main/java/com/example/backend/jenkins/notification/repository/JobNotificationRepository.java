package com.example.backend.jenkins.notification.repository;

import com.example.backend.jenkins.job.model.PipelineVersion;
import com.example.backend.jenkins.notification.model.JobNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobNotificationRepository extends JpaRepository<JobNotification, UUID> {

    void deleteJobNotificationByPipelineVersion(PipelineVersion pipelineVersion);
}
