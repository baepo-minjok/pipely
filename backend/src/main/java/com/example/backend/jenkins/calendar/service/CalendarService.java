package com.example.backend.jenkins.calendar.service;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.build.service.BuildService;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarBuildResDto;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarErrorResDto;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final BuildService buildService;

    public List<CalendarBuildResDto> toCalendarBuildResDtoList(List<Map<String, Object>> builds) {
        return builds.stream()
                .map(this::mapToCalendarBuildResDto)
                .toList();
    }

    private CalendarBuildResDto mapToCalendarBuildResDto(Map<String, Object> build) {
        int buildNumber = (Integer) build.get("number");
        String result = (String) build.get("result");
        long timestamp = ((Number) build.get("timestamp")).longValue();
        long duration = ((Number) build.get("duration")).longValue();

        String jobName = "unknown";
        String buildUrl = (String) build.get("url");
        if (buildUrl != null) {
            String[] parts = buildUrl.split("/");
            for (int i = 0; i < parts.length; i++) {
                if ("job".equals(parts[i]) && i + 1 < parts.length) {
                    jobName = parts[i + 1];
                    break;
                }
            }
        }

        return CalendarBuildResDto.builder()
                .title("[" + result + "] " + jobName + " #" + buildNumber)
                .start(timestamp)
                .end(timestamp + duration)
                .status(result)
                .jobName(jobName)
                .buildNumber(buildNumber)
                .build();
    }

    public List<CalendarEvent> mergeEvents(List<? extends CalendarEvent>... eventGroups) {
        return List.of(eventGroups).stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public Map<String, Object> parseJsonToMap(String json) {
        try {
            return new ObjectMapper().readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.JENKINS_BUILD_HISTORY_PARSE_ERROR);
        }
    }

    public List<CalendarBuildResDto> getBuildCalendar(UUID pipelineId){
        String json = buildService.JenkinsGetResponse(pipelineId);

        Map<String, Object> parsed = parseJsonToMap(json);

        List<Map<String, Object>> builds = (List<Map<String, Object>>) parsed.get("builds");
        return toCalendarBuildResDtoList(builds);
    }

    public List<CalendarErrorResDto> getErrorCalendar(UUID pipelineId) {
        // 1. Jenkins JSON 응답 받아오기
        String json = buildService.JenkinsGetResponse(pipelineId);
        Map<String, Object> parsed = parseJsonToMap(json);

        // 2. builds 추출
        List<Map<String, Object>> builds = (List<Map<String, Object>>) parsed.get("builds");

        // 3. FAILURE만 골라서 매핑
        return builds.stream()
                .filter(b -> "FAILURE".equals(b.get("result")))
                .map(this::mapToErrorResDto)
                .collect(Collectors.toList());
    }

    private CalendarErrorResDto mapToErrorResDto(Map<String, Object> build) {
        int buildNumber = (Integer) build.get("number");
        String message = "원인 미상";
        String failedStage = "Unknown";
        String result = (String) build.get("result");
        long timestamp = ((Number) build.get("timestamp")).longValue();
        long duration = ((Number) build.get("duration")).longValue();

        // 에러 메시지와 실패 단계 추출 (옵셔널)
        List<Map<String, Object>> actions = (List<Map<String, Object>>) build.get("actions");
        if (actions != null) {
            for (Map<String, Object> action : actions) {
                if (action != null && action.containsKey("causes")) {
                    message = ((List<Map<String, Object>>) action.get("causes")).stream()
                            .map(c -> (String) c.getOrDefault("shortDescription", ""))
                            .filter(desc -> !desc.isEmpty())
                            .findFirst().orElse(message);
                }
            }
        }

        return CalendarErrorResDto.builder()
                .title("[ERROR] #" + buildNumber)
                .start(timestamp)
                .end(timestamp + duration)
                .message(message)
                .failedStage(failedStage)
                .buildNumber(buildNumber)
                .build();
    }


}

