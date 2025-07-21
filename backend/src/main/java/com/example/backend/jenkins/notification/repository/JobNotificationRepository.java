package com.example.backend.jenkins.notification.repository;

import com.example.backend.jenkins.notification.model.JobNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobNotificationRepository extends JpaRepository<JobNotification, String> {

    List<JobNotification> findByPipelineId(UUID pipelineId);

    List<JobNotification> findByPipelineIdAndShouldNotifyTrue(UUID pipelineId);
}
