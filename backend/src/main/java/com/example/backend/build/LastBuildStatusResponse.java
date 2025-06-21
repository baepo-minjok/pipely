package com.example.backend.build;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class LastBuildStatusResponse
{

    private String jobName;
    private int buildNumber;
    private String status; // SUCCESS, FAILURE
    private String durationStr; // 4.5초
    private String startedAt; // 2025-06-19 17:23:13
    private String triggeredBy;
    private String buildUrl;

    public static LastBuildStatusResponse parseLastBuild(Map<String, Object> body) {
        String fullName = (String) body.get("fullDisplayName"); // ex: "woojin_test #42"
        String jobName = fullName.split(" ")[0];
        int buildNumber = (Integer) body.get("number");
        String status = (String) body.get("result");
        long durationMs = ((Number) body.get("duration")).longValue();
        long timestamp = ((Number) body.get("timestamp")).longValue();
        String buildUrl = (String) body.get("url");

        // 실행 시간 문자열로 변환
        String durationStr = String.format("%.1f초", durationMs / 1000.0);

        // 시작 시각 포맷
        String startedAt = Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 실행자 추출
        String triggeredBy = "unknown";
        List<Map<String, Object>> actions = (List<Map<String, Object>>) body.get("actions");
        for (Map<String, Object> action : actions) {
            if (action != null && "hudson.model.CauseAction".equals(action.get("_class"))) {
                List<Map<String, Object>> causes = (List<Map<String, Object>>) action.get("causes");
                if (causes != null && !causes.isEmpty()) {
                    triggeredBy = (String) causes.get(0).get("userName");
                    break;
                }
            }
        }

        // 파라미터 추출


        return LastBuildStatusResponse.builder()
                .jobName(jobName)
                .buildNumber(buildNumber)
                .status(status)
                .durationStr(durationStr)
                .startedAt(startedAt)
                .triggeredBy(triggeredBy)
                .buildUrl(buildUrl)
                .build();
    }




}
