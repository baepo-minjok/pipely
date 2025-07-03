package com.example.backend.jenkins.job.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class JobNotificationId implements Serializable {
    private UUID id;
    private String name;
    private LocalDateTime jobCreatedAt;
}

