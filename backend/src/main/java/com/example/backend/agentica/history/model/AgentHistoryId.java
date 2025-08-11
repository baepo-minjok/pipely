package com.example.backend.agentica.history.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@Embeddable
public class AgentHistoryId implements Serializable {
    private UUID userId;
    private String threadId;
    private String chatId;

}
