package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.pipeline.PipelineScript;
import com.example.backend.jenkins.job.model.pipeline.PipelineScriptHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PipelineScriptHistoryRepository extends JpaRepository<PipelineScriptHistory, Long> {
    @Query("SELECT MAX(h.version) FROM PipelineScriptHistory h WHERE h.pipelineScript = :ps")
    Integer findMaxVersionByPipelineScript(@Param("ps") PipelineScript pipelineScript);
}
