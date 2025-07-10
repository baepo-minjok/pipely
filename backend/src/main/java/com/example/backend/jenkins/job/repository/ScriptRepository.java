package com.example.backend.jenkins.job.repository;

import com.example.backend.jenkins.job.model.Script;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScriptRepository extends JpaRepository<Script, UUID> {
}
