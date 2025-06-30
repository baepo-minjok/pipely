package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.FreeStyle;
import com.example.backend.jenkins.job.model.FreeStyleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FreeStyleHistoryRepository extends JpaRepository<FreeStyleHistory, UUID> {
    @Query("SELECT MAX(h.version) FROM FreeStyleHistory h WHERE h.freeStyle = :fs")
    Integer findMaxVersionByFreeStyle(@Param("fs") FreeStyle freeStyle);

    @Query("""
               SELECT h
                 FROM FreeStyleHistory h
            JOIN FETCH h.freeStyle fs
            JOIN FETCH fs.jenkinsInfo ji
                WHERE h.id = :freeStyleHistoryId
            """)
    Optional<FreeStyleHistory> findAllWithFreeStyleAndJenkinsInfoByFreeStyleHistoryId(@Param("freeStyleHistoryId") UUID freeStyleHistoryId);
}
