package com.example.backend.jenkins.info.repository;

import com.example.backend.auth.user.model.Users;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JenkinsInfoRepository extends JpaRepository<JenkinsInfo, UUID> {

    List<JenkinsInfo> findByUser(Users user);

    Optional<JenkinsInfo> findByUserAndJenkinsId(Users user, String jenkinsId);

    void deleteByUser(Users user);

    Optional<JenkinsInfo> findByUserId(UUID userId);
}