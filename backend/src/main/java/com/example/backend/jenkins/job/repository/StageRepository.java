package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StageRepository extends JpaRepository<Stage, UUID> {
    @Modifying
    @Query("delete from Stage s where s.pipeline.id = :pipelineId")
    void deleteByPipelineId(@Param("pipelineId") UUID pipelineId);
}
