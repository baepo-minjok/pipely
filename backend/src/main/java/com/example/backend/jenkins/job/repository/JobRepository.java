package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.Job;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends CrudRepository<Job, UUID> {

    @Query("""
            SELECT j
              FROM Job j
              JOIN FETCH j.jenkinsInfo
              WHERE j.id = :jobId
            """)
    Optional<Job> findJenkinsInfoById(@Param("jobId") UUID jobId);
}
