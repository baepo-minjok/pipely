package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.FreeStyle;
import com.example.backend.jenkins.job.model.pipeline.Pipeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PipelineRepository extends JpaRepository<Pipeline, UUID> {


    @Query("""
            SELECT pl
                FROM Pipeline pl
                JOIN FETCH pl.jenkinsInfo
                WHERE pl.id = :pipelineId
            """)
    Optional<Pipeline> findPipelineById(@Param("pipelineId") UUID pipelineId);

    Optional<Pipeline> findByJenkinsInfoIdAndId(UUID id, UUID jobId);
}
