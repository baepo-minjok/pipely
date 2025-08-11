package com.example.backend.agentica.history.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "agent_history")
public class AgentHistory {
    @EmbeddedId
    private AgentHistoryId id;

    @Type(JsonType.class)
    @Column(name = "json", columnDefinition = "JSON", nullable = false)
    private JsonNode json;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
