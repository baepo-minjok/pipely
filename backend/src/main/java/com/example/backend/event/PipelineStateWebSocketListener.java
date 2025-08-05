package com.example.backend.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PipelineStateWebSocketListener {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handlePipelineStateChanged(PipelineStateChangedEvent event) {
        messagingTemplate.convertAndSendToUser(
                event.getEmail(),
                "/queue/alert",
                event.getChangedState()
        );
    }
}
