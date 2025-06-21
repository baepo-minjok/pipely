package com.example.backend.job.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class JobScheduleId implements Serializable {
    private Integer createdBy;
    private LocalDateTime createdAt;
}