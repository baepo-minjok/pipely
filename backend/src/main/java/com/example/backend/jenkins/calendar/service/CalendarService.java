package com.example.backend.jenkins.calendar.service;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.build.service.BuildService;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarBuildResDto;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarErrorResDto;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarEvent;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarEventRes;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarSummaryRes;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.repository.JenkinsInfoRepository;
import com.example.backend.service.HttpClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final BuildService buildService;
    private final JenkinsInfoRepository jenkinsInfoRepository;
    private final HttpClientService httpClientService;

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

        String jobName = extractJobName(build);

        return CalendarBuildResDto.builder()
                .title("[" + result + "] " + jobName + " #" + buildNumber)
                .start(formatTimestamp(timestamp))
                .end(formatTimestamp(timestamp + duration))
                .status(result)
                .jobName(jobName)
                .buildNumber(buildNumber)
                .duration(formatDuration(duration))
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
        String json = buildService.JenkinsGetResponse(pipelineId);
        Map<String, Object> parsed = parseJsonToMap(json);
        List<Map<String, Object>> builds = (List<Map<String, Object>>) parsed.get("builds");

        return builds.stream()
                .filter(b -> "FAILURE".equals(b.get("result")))
                .map(this::mapToErrorResDto)
                .collect(Collectors.toList());
    }

    private CalendarErrorResDto mapToErrorResDto(Map<String, Object> build) {
        int buildNumber = (Integer) build.get("number");
        String result = (String) build.get("result");
        long timestamp = ((Number) build.get("timestamp")).longValue();
        long duration = ((Number) build.get("duration")).longValue();

        String message = "원인 미상";
        String failedStage = "Unknown";

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
                .start(formatTimestamp(timestamp))
                .end(formatTimestamp(timestamp + duration))
                .message(message)
                .failedStage(failedStage)
                .buildNumber(buildNumber)
                .duration(formatDuration(duration))
                .build();
    }

    private static String formatTimestamp(long millis) {
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String extractJobName(Map<String, Object> build) {
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
        return jobName;
    }

    private static String formatDuration(long millis) {
        if (millis < 1000) {
            return String.format("%.2f초", millis / 1000.0);
        }

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;

        if (minutes > 0) {
            return minutes + "분 " + remainingSeconds + "초";
        } else {
            return remainingSeconds + "초";
        }
    }

    public List<CalendarEventRes> getCalendarEventList(Users user, UUID infoId) {
        JenkinsInfo info = jenkinsInfoRepository.findById(infoId)
                .filter(i -> i.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));

        List<CalendarEventRes> result = new ArrayList<>();

        List<String> jobNames = getAllJobNames(info);

        for (String jobName : jobNames) {
            List<Map<String, Object>> builds = getBuildsForJob(info, jobName);

            for (Map<String, Object> build : builds) {
                int buildNumber = (Integer) build.get("number");
                long ts = ((Number) build.get("timestamp")).longValue();
                String resultStr = (String) build.get("result");

                String type = "FAILURE".equals(resultStr) ? "ERROR" : "BUILD";

                result.add(CalendarEventRes.builder()
                        .type(type)
                        .jobName(jobName)
                        .buildNumber(buildNumber)
                        .start(formatTimestamp(ts))
                        .build());
            }
        }

        return result;
    }

    private List<String> getAllJobNames(JenkinsInfo info) {
        String url = info.getUri() + "/api/json?tree=jobs[name]";
        Map<?, ?> response = httpClientService.exchange(url, HttpMethod.GET, buildHttpEntity(info), Map.class);
        List<Map<String, Object>> jobs = (List<Map<String, Object>>) response.get("jobs");
        return jobs.stream()
                .map(job -> (String) job.get("name"))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getBuildsForJob(JenkinsInfo info, String jobName) {
        String url = info.getUri() + "/job/" + jobName + "/api/json?tree=builds[number,timestamp,result]";
        Map<?, ?> response = httpClientService.exchange(url, HttpMethod.GET, buildHttpEntity(info), Map.class);
        Object buildsObj = response.get("builds");

        if (buildsObj instanceof List<?>) {
            return ((List<?>) buildsObj).stream()
                    .filter(e -> e instanceof Map)
                    .map(e -> (Map<String, Object>) e)
                    .toList();
        }

        return new ArrayList<>();

    }

    private HttpEntity<?> buildHttpEntity(JenkinsInfo info) {
        return new HttpEntity<>(httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON));
    }

    public List<CalendarEventRes> getEventsByDate(Users user, UUID infoId, String date) {
        JenkinsInfo info = jenkinsInfoRepository.findById(infoId)
                .filter(i -> i.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));

        List<CalendarEventRes> result = new ArrayList<>();

        for (String jobName : getAllJobNames(info)) {
            for (Map<String, Object> build : getBuildsForJob(info, jobName)) {
                String ts = formatTimestamp(((Number) build.get("timestamp")).longValue()); // yyyy-MM-dd HH:mm:ss
                if (!ts.startsWith(date)) continue;

                result.add(CalendarEventRes.builder()
                        .type("FAILURE".equals(build.get("result")) ? "ERROR" : "BUILD")
                        .jobName(jobName)
                        .buildNumber((Integer) build.get("number"))
                        .start(ts)
                        .build());
            }
        }

        return result;
    }

    public Map<String, CalendarSummaryRes> getCalendarSummary(Users user, UUID infoId) {
        JenkinsInfo info = jenkinsInfoRepository.findById(infoId)
                .filter(i -> i.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));

        Map<String, CalendarSummaryRes> summaryMap = new HashMap<>();

        for (String jobName : getAllJobNames(info)) {
            for (Map<String, Object> build : getBuildsForJob(info, jobName)) {
                long ts = ((Number) build.get("timestamp")).longValue();
                String result = (String) build.get("result");
                String date = formatDateOnly(ts); // yyyy-MM-dd

                CalendarSummaryRes existing = summaryMap.getOrDefault(date, new CalendarSummaryRes(0, 0));

                if ("FAILURE".equals(result)) {
                    existing.setErrorCount(existing.getErrorCount() + 1);
                } else {
                    existing.setBuildCount(existing.getBuildCount() + 1);
                }

                summaryMap.put(date, existing);
            }
        }

        return summaryMap;
    }

    private String formatDateOnly(long millis) {
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public CalendarSummaryRes getCalendarSummaryByDate(Users user, UUID infoId, String date) {
        JenkinsInfo info = jenkinsInfoRepository.findById(infoId)
                .filter(i -> i.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));

        int build = 0;
        int error = 0;

        for (String jobName : getAllJobNames(info)) {
            for (Map<String, Object> buildObj : getBuildsForJob(info, jobName)) {
                long ts = ((Number) buildObj.get("timestamp")).longValue();
                String result = (String) buildObj.get("result");
                String tsDate = formatDateOnly(ts); // "yyyy-MM-dd"

                if (!tsDate.equals(date)) continue;

                if ("FAILURE".equals(result)) error++;
                else build++;
            }
        }

        return CalendarSummaryRes.builder()
                .buildCount(build)
                .errorCount(error)
                .build();
    }


}
