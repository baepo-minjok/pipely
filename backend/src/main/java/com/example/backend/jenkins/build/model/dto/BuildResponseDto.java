package com.example.backend.jenkins.build.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.jsoup.nodes.Element;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BuildResponseDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Jenkins 빌드 정보 응답 DTO")
    public static class BuildInfo {

        @Schema(description = "Job 이름", example = "sample-job")
        private String jobName;

        @Schema(description = "빌드 번호", example = "42")
        private int buildNumber;

        @Schema(description = "빌드 상태", example = "SUCCESS")
        private String status;

        @Schema(description = "현재 빌드 중 여부", example = "false")
        private boolean building;

        @Schema(description = "빌드 소요 시간 문자열", example = "12.3초")
        private String durationStr;

        @Schema(description = "시작 시간 (한국시간 기준)", example = "2025-07-10 13:30:00")
        private String startedAt;

        @Schema(description = "빌드 트리거 사용자", example = "admin")
        private String triggeredBy;

        @Schema(description = "빌드 URL", example = "http://jenkins.example.com/job/sample-job/42/")
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

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    @Schema(description = "실시간 로그 응답 DTO")
    public static class BuildStreamLogDto {

        @Schema(description = "로그 라인 리스트", example = "[\"Started by user admin\", \"Building...\"]")
        private List<String> log;

        public static BuildStreamLogDto getStreamLog(String body) {
            List<String> lines = Arrays.asList(body.split("\\r?\\n"));
            return BuildStreamLogDto.builder()
                    .log(lines)
                    .build();
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    @Schema(description = "빌드 로그 응답 DTO")
    public static class BuildLogDto {

        @Schema(description = "로그 라인 리스트", example = "[\"Compiling...\", \"Build finished successfully.\"]")
        private List<String> log;

        public static BuildLogDto getLog(Element pre) {
            String rawLog = pre.text();
            List<String> lines = List.of(rawLog.split("\n"));
            return BuildLogDto.builder()
                    .log(lines)
                    .build();
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    @Schema(description = "Job의 스테이지 목록 응답 DTO")
    public static class Stage {

        @Schema(description = "스테이지 목록", example = "[\"BUILD\", \"TEST\", \"DEPLOY\"]")
        private List<String> stage = new ArrayList<>();

        public static Stage getStage(List<String> body) {
            return Stage.builder().stage(body).build();
        }
    }
}
