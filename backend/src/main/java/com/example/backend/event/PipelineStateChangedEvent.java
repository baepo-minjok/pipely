package com.example.backend.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PipelineStateChangedEvent {
    private String email;
    private ChangedState changedState;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ChangedState {

        private String name;
        private String state;
    }
}
