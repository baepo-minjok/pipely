package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.FreeStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FreeStyleRepository extends JpaRepository<FreeStyle, UUID> {

    @Query("""
            SELECT fs
                FROM FreeStyle fs
                JOIN FETCH fs.jenkinsInfo
                WHERE fs.id = :freeStyleId
            """)
    Optional<FreeStyle> findFreeStyleById(@Param("freeStyleId") UUID freeStyleId);
}
