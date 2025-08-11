package com.example.backend.agentica.history.controller;

import com.example.backend.agentica.history.model.dto.AgentHistoryDto;
import com.example.backend.agentica.history.service.AgentHistoryService;
import com.example.backend.auth.user.model.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/agentica/history")
public class AgentHistoryController {
    private final AgentHistoryService service;

    @GetMapping({"/me", "/me/{threadId}"})
    public List<AgentHistoryDto> load(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @PathVariable(required = false) String threadId
    ) {
        return service.load(user.getId(), threadId == null ? "default" : threadId);
    }

    @PostMapping({"/me", "/me/{threadId}"})
    public ResponseEntity<Void> upsert(
            @AuthenticationPrincipal(expression = "userEntity") Users user,
            @PathVariable(required = false) String threadId,
            @RequestBody List<AgentHistoryDto> prompts
    ) {
        service.upsertAll(user.getId(), threadId == null ? "default" : threadId, prompts);
        return ResponseEntity.ok().build();
    }
    
}
