package com.example.backend.jenkins.calendar.service;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarEventRes;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarSummaryRes;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarEventGroupRes;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.repository.JenkinsInfoRepository;
import com.example.backend.service.HttpClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final JenkinsInfoRepository jenkinsInfoRepository;
    private final HttpClientService httpClientService;

    /**
     * 특정 날짜의 이벤트 리스트 조회
     */
    public List<CalendarEventRes> getEventsByDate(Users user, UUID infoId, String date) {
        JenkinsInfo info = getValidJenkinsInfo(user, infoId);
        List<CalendarEventRes> result = new ArrayList<>();

        for (String jobName : getAllJobNames(info)) {
            for (Map<String, Object> build : getBuildsForJob(info, jobName)) {
                String ts = formatTimestamp(((Number) build.get("timestamp")).longValue());
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

    /**
     * 특정 날짜의 요약 통계 조회
     */
    public CalendarSummaryRes getCalendarSummaryByDate(Users user, UUID infoId, String date) {
        JenkinsInfo info = getValidJenkinsInfo(user, infoId);
        int build = 0;
        int error = 0;

        for (String jobName : getAllJobNames(info)) {
            for (Map<String, Object> buildObj : getBuildsForJob(info, jobName)) {
                long ts = ((Number) buildObj.get("timestamp")).longValue();
                String result = (String) buildObj.get("result");
                String tsDate = formatDateOnly(ts);

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

    public List<CalendarEventGroupRes> getEventsByMonth(Users user, UUID infoId, int year, int month) {
        JenkinsInfo info = getValidJenkinsInfo(user, infoId);
        Map<String, List<CalendarEventRes>> grouped = new HashMap<>();

        for (String jobName : getAllJobNames(info)) {
            for (Map<String, Object> build : getBuildsForJob(info, jobName)) {
                long ts = ((Number) build.get("timestamp")).longValue();
                LocalDate buildDate = Instant.ofEpochMilli(ts)
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .toLocalDate();

                if (buildDate.getYear() == year && buildDate.getMonthValue() == month) {
                    String dateKey = buildDate.toString();
                    grouped.computeIfAbsent(dateKey, k -> new ArrayList<>())
                            .add(CalendarEventRes.builder()
                                    .type("FAILURE".equals(build.get("result")) ? "ERROR" : "BUILD")
                                    .jobName(jobName)
                                    .buildNumber((Integer) build.get("number"))
                                    .start(formatTimestamp(ts))
                                    .build());
                }
            }
        }

        return grouped.entrySet().stream()
                .map(e -> CalendarEventGroupRes.builder()
                        .date(e.getKey())
                        .events(e.getValue())
                        .build())
                .sorted(Comparator.comparing(CalendarEventGroupRes::getDate))
                .toList();
    }



    /**
     * 사용자 권한 검증 포함한 JenkinsInfo 조회
     */
    private JenkinsInfo getValidJenkinsInfo(Users user, UUID infoId) {
        return jenkinsInfoRepository.findById(infoId)
                .filter(i -> i.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));
    }

    // ──────────────────────────────
    // 공통 / 유틸 메서드
    // ──────────────────────────────


    /**
     * timestamp → yyyy-MM-dd HH:mm:ss 포맷 문자열로 변환
     */
    private String formatTimestamp(long millis) {
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * timestamp → yyyy-MM-dd 포맷
     */
    private String formatDateOnly(long millis) {
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * Jenkins 서버에서 모든 Job 이름 목록 조회
     */
    private List<String> getAllJobNames(JenkinsInfo info) {
        String url = info.getUri() + "/api/json?tree=jobs[name]";
        Map<?, ?> response = httpClientService.exchange(url, HttpMethod.GET, buildHttpEntity(info), Map.class);
        List<Map<String, Object>> jobs = (List<Map<String, Object>>) response.get("jobs");
        List<String> names = new ArrayList<>();
        for (Map<String, Object> job : jobs) {
            names.add((String) job.get("name"));
        }
        return names;
    }

    /**
     * 특정 Job의 모든 빌드 이력 조회
     */
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

    /**
     * Jenkins API 요청 시 필요한 인증 헤더 포함한 HttpEntity 생성
     */
    private HttpEntity<?> buildHttpEntity(JenkinsInfo info) {
        return new HttpEntity<>(httpClientService.buildHeaders(info, MediaType.APPLICATION_JSON));
    }
}
