package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.PipelineVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PipelineVersionRepository extends JpaRepository<PipelineVersion, UUID> {
    Optional<PipelineVersion> findTopByPipelineOrderByCreatedAtDesc(Pipeline pipeline);

    Optional<PipelineVersion> findTopByScriptIdOrderByCreatedAtDesc(UUID scriptId);

    Optional<PipelineVersion> findByScriptId(UUID scriptId);
}