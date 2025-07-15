package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.PipelineVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PipelineVersionRepository extends JpaRepository<PipelineVersion, UUID> {
}