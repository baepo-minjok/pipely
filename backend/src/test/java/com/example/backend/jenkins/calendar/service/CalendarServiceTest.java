package com.example.backend.jenkins.calendar.service;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.CustomException;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarEventRes;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarSummaryRes;
import com.example.backend.jenkins.info.repository.JenkinsInfoRepository;
import com.example.backend.service.HttpClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalendarServiceTest {

    private CalendarService calendarService;
    private JenkinsInfoRepository jenkinsInfoRepository;
    private HttpClientService httpClientService;

    private Users mockUser;
    private JenkinsInfo mockInfo;
    private UUID infoId;

    @BeforeEach
    void setUp() {
        jenkinsInfoRepository = mock(JenkinsInfoRepository.class);
        httpClientService = mock(HttpClientService.class);
        calendarService = new CalendarService(jenkinsInfoRepository, httpClientService);

        mockUser = Users.builder().id(UUID.randomUUID()).build();
        mockInfo = JenkinsInfo.builder().id(UUID.randomUUID()).user(mockUser).uri("http://jenkins.test").build();
        infoId = mockInfo.getId();
    }

    @Test
    void getEventsByDate_SUCCESS_and_FAILURE_정상매핑() {
        // given
        when(jenkinsInfoRepository.findById(infoId)).thenReturn(Optional.of(mockInfo));
        when(httpClientService.exchange(
                contains("/api/json?tree=jobs[name]"),
                eq(HttpMethod.GET),
                any(),
                eq(Map.class)
        )).thenReturn(Map.of("jobs", List.of(Map.of("name", "pipeline-test"))));

        when(httpClientService.exchange(
                contains("/job/pipeline-test/api/json?tree=builds[number,timestamp,result]"),
                eq(HttpMethod.GET),
                any(),
                eq(Map.class)
        )).thenReturn(Map.of("builds", List.of(
                Map.of("number", 1, "timestamp", 1753305600000L, "result", "SUCCESS"),
                Map.of("number", 2, "timestamp", 1753305600000L, "result", "FAILURE")
        )));

        // when
        List<CalendarEventRes> result = calendarService.getEventsByDate(mockUser, infoId, "2025-07-24");

        // then
        assertThat(result).hasSize(2);

        CalendarEventRes buildEvent = result.stream()
                .filter(e -> e.getBuildNumber() == 1)
                .findFirst()
                .orElseThrow();

        CalendarEventRes errorEvent = result.stream()
                .filter(e -> e.getBuildNumber() == 2)
                .findFirst()
                .orElseThrow();

        assertThat(buildEvent.getType()).isEqualTo("BUILD");
        assertThat(errorEvent.getType()).isEqualTo("ERROR");
    }

    @Test
    void getSummaryByDate_통계테스트() {
        // given
        when(jenkinsInfoRepository.findById(infoId)).thenReturn(Optional.of(mockInfo));
        when(httpClientService.exchange(
                contains("/api/json?tree=jobs[name]"),
                eq(HttpMethod.GET),
                any(),
                eq(Map.class)
        )).thenReturn(Map.of("jobs", List.of(Map.of("name", "pipeline-test"))));

        when(httpClientService.exchange(
                contains("/job/pipeline-test/api/json?tree=builds[number,timestamp,result]"),
                eq(HttpMethod.GET),
                any(),
                eq(Map.class)
        )).thenReturn(Map.of("builds", List.of(
                Map.of("number", 42, "timestamp", 1753305600000L, "result", "FAILURE"),
                Map.of("number", 43, "timestamp", 1753305600000L, "result", "SUCCESS")
        )));

        // when
        CalendarSummaryRes summary = calendarService.getCalendarSummaryByDate(mockUser, infoId, "2025-07-24");

        // then
        assertThat(summary.getBuildCount()).isEqualTo(1);
        assertThat(summary.getErrorCount()).isEqualTo(1);
    }

    @Test
    void getEventsByDate_권한없을때_예외() {
        // given
        UUID 다른유저Id = UUID.randomUUID();
        Users 다른유저 = Users.builder().id(다른유저Id).build();
        mockInfo.setUser(다른유저);

        when(jenkinsInfoRepository.findById(infoId)).thenReturn(Optional.of(mockInfo));

        // when & then
        assertThatThrownBy(() -> calendarService.getEventsByDate(mockUser, infoId, "2025-07-24"))
                .isInstanceOf(CustomException.class);
    }

}
