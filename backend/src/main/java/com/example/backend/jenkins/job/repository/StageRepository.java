package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StageRepository extends JpaRepository<Stage, String> {
}
