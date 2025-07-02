package com.example.backend.jenkins.job.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class JobNotificationId implements Serializable {
    private String name;
    private LocalDateTime jobCreatedAt;
}

