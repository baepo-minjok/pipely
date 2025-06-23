package com.example.backend.build.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BuildResponseDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BuildInfo {
        private String jobName;
        private int buildNumber;
        private String status;
        private boolean building;
        private String durationStr;
        private String startedAt;
        private String triggeredBy;
        private String buildUrl;

        public static BuildInfo from(Map<String, Object> build) {
            long timestamp = ((Number) build.get("timestamp")).longValue();
            long durationMs = ((Number) build.get("duration")).longValue();

            String triggeredBy = "unknown";
            List<Map<String, Object>> actions = (List<Map<String, Object>>) build.get("actions");
            if (actions != null) {
                for (Map<String, Object> action : actions) {
                    if (action == null) continue;
                    List<Map<String, Object>> causes = (List<Map<String, Object>>) action.get("causes");
                    if (causes != null) {
                        for (Map<String, Object> cause : causes) {
                            if (cause.containsKey("userName")) {
                                triggeredBy = (String) cause.get("userName");
                                break;
                            }
                        }
                    }
                }
            }

            String buildUrl = (String) build.get("url");
            String jobName = "unknown";
            if (buildUrl != null) {
                String[] parts = buildUrl.split("/");
                for (int i = 0; i < parts.length; i++) {
                    if ("job".equals(parts[i]) && i + 1 < parts.length) {
                        jobName = parts[i + 1];
                        break;
                    }
                }
            }

            return BuildInfo.builder()
                    .jobName(jobName)
                    .buildNumber((Integer) build.get("number"))
                    .status((String) build.get("result"))
                    .building((Boolean) build.getOrDefault("building", false))
                    .durationStr(String.format("%.1f초", durationMs / 1000.0))
                    .startedAt(Instant.ofEpochMilli(timestamp)
                            .atZone(ZoneId.of("Asia/Seoul"))
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .triggeredBy(triggeredBy)
                    .buildUrl(buildUrl)
                    .build();
        }

        public static BuildInfo latestFrom(Map<String, Object> body) {
            List<Map<String, Object>> builds = (List<Map<String, Object>>) body.get("builds");

            return builds.stream()
                    .max(Comparator.comparingInt(b -> (Integer) b.get("number")))
                    .map(BuildInfo::from)
                    .orElseThrow(() -> new IllegalArgumentException("빌드가 존재하지 않습니다."));
        }

        public static List<BuildInfo> listFrom(Map<String, Object> body) {
            List<Map<String, Object>> builds = (List<Map<String, Object>>) body.get("builds");
            return builds.stream()
                    .map(BuildInfo::from)
                    .collect(Collectors.toList());
        }
    }


}
