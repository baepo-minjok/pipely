package com.example.backend.jenkins.calendar.service;

import com.example.backend.auth.user.model.Users;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.repository.JenkinsInfoRepository;
import com.example.backend.service.HttpClientService;
import org.junit.jupiter.api.BeforeEach;

import java.util.*;

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

}
