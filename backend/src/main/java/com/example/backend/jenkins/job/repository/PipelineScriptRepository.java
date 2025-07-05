package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.pipeline.PipelineScript;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineScriptRepository extends JpaRepository<PipelineScript, Long> {
}
