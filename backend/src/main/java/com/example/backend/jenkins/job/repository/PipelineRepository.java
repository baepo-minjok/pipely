package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.Pipeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PipelineRepository extends JpaRepository<Pipeline, UUID> {

    @Query("""
            select p
            from Pipeline p
            left join fetch p.jenkinsInfo ji
            left join fetch p.script s
            where p.id = :id
            """)
    Optional<Pipeline> findWithInfoAndScriptById(@Param("id") UUID id);

    @Query("""
            select p
            from Pipeline p
            left join fetch p.jenkinsInfo ji
            left join fetch p.script s
            left join fetch ji.user
            where p.id = :id
            """)
    Optional<Pipeline> findWithInfoAndScriptAndUserById(@Param("id") UUID id);


    @Query("""
            select p
            from Pipeline p
            left join fetch p.script s
            where p.jenkinsInfo.id = :jenkinsInfoId
              and p.isDeleted = false
            """)
    List<Pipeline> findActiveWithScriptByJenkinsInfoId(@Param("jenkinsInfoId") UUID jenkinsInfoId);

    @Query("""
            select p
            from Pipeline p
            left join fetch p.script s
            where p.jenkinsInfo.id = :jenkinsInfoId
              and p.isDeleted = true
            """)
    List<Pipeline> findDeletedWithScriptByJenkinsInfoId(@Param("jenkinsInfoId") UUID jenkinsInfoId);

    Optional<Pipeline> findByJenkinsInfoIdAndName(UUID jenkinsInfoId, String name);
}
