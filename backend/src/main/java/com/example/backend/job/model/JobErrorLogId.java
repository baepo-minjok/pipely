package com.example.backend.job.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class JobErrorLogId implements Serializable {
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
}
