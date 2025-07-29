package com.example.backend.jenkins.calendar.service;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.CustomException;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto;
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
    void getEventsByMonth_그룹핑정상() {
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
                Map.of("number", 42, "timestamp", 1753305600000L, "result", "SUCCESS") // 2025-07-24
        )));

        // when
        List<CalendarResponseDto.CalendarEventGroupRes> result = calendarService.getEventsByMonth(mockUser, infoId, 2025, 7);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDate()).isEqualTo("2025-07-24");
        assertThat(result.get(0).getEvents()).hasSize(1);
        assertThat(result.get(0).getEvents().get(0).getJobName()).isEqualTo("pipeline-test");
        assertThat(result.get(0).getEvents().get(0).getType()).isEqualTo("BUILD");
    }

}
