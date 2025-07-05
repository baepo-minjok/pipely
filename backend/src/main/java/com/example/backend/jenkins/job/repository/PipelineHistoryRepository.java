package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.pipeline.Pipeline;
import com.example.backend.jenkins.job.model.pipeline.PipelineHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PipelineHistoryRepository extends JpaRepository<PipelineHistory, UUID> {
    @Query("SELECT MAX(h.version) FROM PipelineHistory h WHERE h.pipeline = :ps")
    Integer findMaxVersionByPipelineScript(@Param("ps") Pipeline pipelineScript);
}
