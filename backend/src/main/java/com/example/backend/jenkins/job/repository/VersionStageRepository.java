package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.PipelineVersion;
import com.example.backend.jenkins.job.model.VersionStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VersionStageRepository extends JpaRepository<VersionStage, UUID> {

    void deleteVersionStageByPipelineVersion(PipelineVersion pipelineVersion);
}
