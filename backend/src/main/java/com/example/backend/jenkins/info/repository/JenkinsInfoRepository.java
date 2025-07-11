package com.example.backend.jenkins.info.repository;

import com.example.backend.auth.user.model.Users;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JenkinsInfoRepository extends JpaRepository<JenkinsInfo, UUID> {

    List<JenkinsInfo> findByUser(Users user);

    @Query("""
                SELECT ji
                FROM JenkinsInfo ji
                LEFT JOIN ji.user u
                WHERE ji.id = :id
            """)
    Optional<JenkinsInfo> findWithUserById(@Param("id") UUID id);

    Optional<JenkinsInfo> findByUserAndJenkinsId(Users user, String jenkinsId);

    void deleteByUser(Users user);
}