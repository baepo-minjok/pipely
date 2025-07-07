package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.FreeStyle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FreeStyleRepository extends JpaRepository<FreeStyle, UUID> {
    Optional<FreeStyle> findByJenkinsInfoIdAndId(UUID jenkinsInfoId, UUID jobId);
}
