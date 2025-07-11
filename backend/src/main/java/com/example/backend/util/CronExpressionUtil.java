package com.example.backend.util;


import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 사용자 친화적인 시간 및 주기 표현을 cron 식으로 변환하는 유틸리티 클래스
 */
@Component
public class CronExpressionUtil {
    // cron 템플릿: 초 분 시 일 월 요일
    private static final String CRON_DAILY_TEMPLATE = "0 %d %d * * ?";
    private static final String CRON_WEEKLY_TEMPLATE = "0 %d %d ? * %s";

    // 한글 오전/오후 패턴
    private static final Pattern KOREAN_TIME_PATTERN = Pattern.compile(
            "(오전|오후)\\s*(\\d{1,2})시\\s*(\\d{1,2})분?"
    );

    // 주기 + (요일) + 시간 패턴: ex) "매주 월,수,금 오후 3시 5분"
    private static final Pattern SCHEDULE_PATTERN = Pattern.compile(
            "^(매일|매주)\\s*(?:([월화수목금토일](?:,\\s*[월화수목금토일])*)(?:요일)?)?\\s*(오전\\s*\\d{1,2}시\\s*\\d{1,2}분?|오후\\s*\\d{1,2}시\\s*\\d{1,2}분?|\\d{1,2}:\\d{1,2})$"
    );

    // 한글 요일 -> Cron 요일 표현
    private static final Map<String, String> DOW_MAP = new HashMap<>();

    static {
        DOW_MAP.put("일", "SUN");
        DOW_MAP.put("월", "MON");
        DOW_MAP.put("화", "TUE");
        DOW_MAP.put("수", "WED");
        DOW_MAP.put("목", "THU");
        DOW_MAP.put("금", "FRI");
        DOW_MAP.put("토", "SAT");
    }

    /**
     * 예) "매일 09:30", "매주 월,수,금 오전 9시 30분"
     */
    public static String toCron(String userSchedule) {
        Matcher schedM = SCHEDULE_PATTERN.matcher(userSchedule.trim());
        if (!schedM.matches()) {
            throw new IllegalArgumentException("지원하지 않는 형식입니다: " + userSchedule);
        }

        String freq = schedM.group(1);
        String days = schedM.group(2);
        String timePart = schedM.group(3);

        // 시간 파싱
        LocalTime time = parseTime(timePart);
        int hour = time.getHour();
        int minute = time.getMinute();

        // cron 생성
        if ("매일".equals(freq)) {
            return String.format(CRON_DAILY_TEMPLATE, minute, hour);
        } else {
            // 매주
            String dowCron;
            if (days == null || days.isEmpty()) {
                // 요일 지정 없으면 월~일 모두
                dowCron = String.join(",", DOW_MAP.values());
            } else {
                String[] tokens = days.split("\\s*,\\s*");
                List<String> cronDays = new ArrayList<>();
                for (String token : tokens) {
                    String abbr = DOW_MAP.get(token);
                    if (abbr == null) throw new IllegalArgumentException("알 수 없는 요일: " + token);
                    cronDays.add(abbr);
                }
                dowCron = String.join(",", cronDays);
            }
            return String.format(CRON_WEEKLY_TEMPLATE, minute, hour, dowCron);
        }
    }

    private static LocalTime parseTime(String input) {
        String trimmed = input.trim();
        // 한글 오전/오후
        Matcher m = KOREAN_TIME_PATTERN.matcher(trimmed);
        if (m.matches()) {
            String amPm = m.group(1);
            int h = Integer.parseInt(m.group(2));
            int mnt = Integer.parseInt(m.group(3));
            if ("오후".equals(amPm) && h < 12) h += 12;
            if ("오전".equals(amPm) && h == 12) h = 0;
            return LocalTime.of(h, mnt);
        }
        throw new IllegalArgumentException("시간을 파싱할 수 없습니다: " + input);
    }

    // 테스트
    public static void main(String[] args) {
        String[] samples = {
                "매일 9:30",
                "매주 3:00",
                "매주 월,수, 목, 금 오후 12시 35분"
        };
        for (String s : samples) {
            try {
                System.out.printf("%s -> %s%n", s, toCron(s));
            } catch (Exception e) {
                System.err.printf("파싱 실패: %s (%s)%n", s, e.getMessage());
            }
        }
    }
}

