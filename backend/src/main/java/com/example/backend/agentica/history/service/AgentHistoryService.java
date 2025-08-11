package com.example.backend.agentica.history.service;

import com.example.backend.agentica.history.model.AgentHistory;
import com.example.backend.agentica.history.model.AgentHistoryId;
import com.example.backend.agentica.history.model.dto.AgentHistoryDto;
import com.example.backend.agentica.history.repository.AgentHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AgentHistoryService {
    private final AgentHistoryRepository repo;

    public AgentHistoryService(AgentHistoryRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<AgentHistoryDto> load(UUID userId, String threadId) {
        return repo.findByIdUserIdAndIdThreadId(userId, threadId)
                .stream().map(AgentHistoryDto::fromEntity).toList();
    }

    /**
     * upsert 배치
     */
    @Transactional
    public void upsertAll(UUID userId, String threadId, List<AgentHistoryDto> dtos) {
        if (dtos == null || dtos.isEmpty()) return;
        List<AgentHistory> list = new ArrayList<>(dtos.size());
        for (AgentHistoryDto d : dtos) {
            AgentHistory e = new AgentHistory();
            AgentHistoryId id = new AgentHistoryId();
            id.setUserId(userId);
            id.setThreadId(threadId);
            id.setChatId(d.json().path("id").asText());

            e.setId(id);
            e.setJson(d.json());
            list.add(e);
        }
        repo.saveAll(list);
    }
}
