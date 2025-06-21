package com.example.backend.job.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class JobVersionId implements Serializable {
    private Integer version;
    private LocalDateTime versionCreatedAt;
}
