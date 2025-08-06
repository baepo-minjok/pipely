package com.example.backend.service;

import com.example.backend.jenkins.build.model.dto.BuildResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class BuildPollingService {
    private final ExecutorService executorService;
    private final Map<String, Future<?>> pollingTasks = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public void sendLog(UUID jobId, int buildNumber, String email, BuildViewCallback viewCallback) {
        String key = jobId.toString();

        if (pollingTasks.containsKey(key) && !pollingTasks.get(key).isDone()) {
            return;
        }

        Future<?> future = executorService.submit(() -> {
            try {
                boolean running = true;
                int waitCount = 0;
                int maxWaitCount = 10;
                int sleepMs = 1500;
                while (running) {
                    BuildResponseDto.BuildStatusDto dto;
                    try {
                        dto = viewCallback.viewBuild(jobId, buildNumber);
                    } catch (Exception e) {
                        dto = null;
                    }

                    if (dto == null) {
                        if (++waitCount > maxWaitCount) {
                            messagingTemplate.convertAndSendToUser(
                                    email, "/queue/build",
                                    BuildResponseDto.BuildStatusDto.builder()
                                            .id(jobId)
                                            .status("QUEUE_TIMEOUT")
                                            .stages(Collections.emptyList())
                                            .log("빌드가 너무 오래 대기 중입니다.")
                                            .build()
                            );
                            break;
                        }
                        sleep(sleepMs);
                        continue;
                    }

                    messagingTemplate.convertAndSendToUser(email, "/queue/build", dto);
                    if (dto.isFinished()) running = false;
                    sleep(sleepMs);
                }
            } finally {
                pollingTasks.remove(key);
            }
        });

        pollingTasks.put(key, future);
    }

    public void shutdown() {
        executorService.shutdown();
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    // 콜백 정의 (람다/메서드 참조용)
    public interface BuildViewCallback {
        BuildResponseDto.BuildStatusDto viewBuild(UUID jobId, int buildNumber);
    }
}
