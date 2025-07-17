package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.PipelineVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PipelineVersionRepository extends JpaRepository<PipelineVersion, UUID> {
    
    Optional<PipelineVersion> findTopByPipelineOrderByCreatedAtDesc(Pipeline pipeline);

    Optional<PipelineVersion> findTopByScriptIdOrderByCreatedAtDesc(UUID scriptId);

    Optional<PipelineVersion> findByScriptId(UUID scriptId);

    @Query("select pv from PipelineVersion pv " +
            "left join fetch pv.script " +
            "left join fetch pv.stageList " +
            "where pv.id = :id")
    Optional<PipelineVersion> findWithScriptAndStageListById(@Param("id") UUID id);

}