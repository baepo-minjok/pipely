package com.example.backend.jenkins.calendar.service;

import com.example.backend.jenkins.build.model.dto.BuildResponseDto.BuildInfo;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarBuildResDto;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {

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
}

