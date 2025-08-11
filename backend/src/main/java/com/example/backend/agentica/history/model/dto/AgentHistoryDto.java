package com.example.backend.agentica.history.model.dto;

import com.example.backend.agentica.history.model.AgentHistory;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

@Builder
public record AgentHistoryDto(JsonNode json) {
    public static AgentHistoryDto fromEntity(AgentHistory agentHistory) {
        return AgentHistoryDto.builder()
                .json(agentHistory.getJson())
                .build();
    }
}
