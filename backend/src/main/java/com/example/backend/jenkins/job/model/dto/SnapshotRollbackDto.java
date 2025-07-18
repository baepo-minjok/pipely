package com.example.backend.jenkins.job.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class SnapshotRollbackDto {
    private UUID pipelineId;
    private UUID versionId;
    private String name;
    private String config;
    private UUID scriptId;
    private boolean isTriggered;
    private String schedule;
    private String description;
}
