package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.pipeline.Pipeline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PipelineRepository extends JpaRepository<Pipeline, UUID> {
}
