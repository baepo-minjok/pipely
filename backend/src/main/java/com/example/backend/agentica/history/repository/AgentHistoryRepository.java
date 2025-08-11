package com.example.backend.agentica.history.repository;

import com.example.backend.agentica.history.model.AgentHistory;
import com.example.backend.agentica.history.model.AgentHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgentHistoryRepository extends JpaRepository<AgentHistory, AgentHistoryId> {
    List<AgentHistory> findByIdUserIdAndIdThreadId(UUID userId, String threadId);
}