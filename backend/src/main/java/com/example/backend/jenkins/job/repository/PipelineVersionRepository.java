package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.PipelineVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PipelineVersionRepository extends JpaRepository<PipelineVersion, UUID> {

    @Query("select pv from PipelineVersion pv " +
            "left join fetch pv.script " +
            "left join fetch pv.stageList " +
            "where pv.id = :id")
    Optional<PipelineVersion> findWithScriptAndStageListById(@Param("id") UUID id);

    @Query("""
            select pv from PipelineVersion pv
            left join fetch pv.pipeline p
            where pv.id = :id
            """)
    Optional<PipelineVersion> findWithPipelineById(@Param("id") UUID id);

    @Query("""
            select pv from PipelineVersion pv
            left join fetch pv.pipeline p
            left join fetch p.jenkinsInfo ji
            left join fetch ji.user
            where pv.id = :id
            """)
    Optional<PipelineVersion> findWithPipelineAndJenkinsInfoAndUserById(@Param("id") UUID id);

}