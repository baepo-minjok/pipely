package com.example.backend.jenkins.calendar.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.service.CustomUserDetails;
import com.example.backend.config.jwt.JwtAuthenticationFilter;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.jenkins.calendar.service.CalendarService;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarEventRes;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarSummaryRes;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(CalendarController.class)
@AutoConfigureMockMvc(addFilters = false)
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CalendarService calendarService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private UUID infoId;
    private Users mockUser;

    @BeforeEach
    void setUp() {
        infoId = UUID.randomUUID();
        mockUser = Users.builder().id(UUID.randomUUID()).build();
    }

    @Test
    @DisplayName("이벤트 조회 API 응답 성공 테스트")
    void getEventsByDate_정상응답() throws Exception {
        // Mocked event data
        List<CalendarEventRes> mockEvents = List.of(
                CalendarEventRes.builder()
                        .type("BUILD")
                        .jobName("pipeline-test")
                        .buildNumber(42)
                        .start("2025-07-24 13:20:00")
                        .build()
        );

        // Mocking the calendar service response
        Mockito.when(calendarService.getEventsByDate(Mockito.any(), Mockito.eq(infoId), Mockito.eq("2025-07-24")))
                .thenReturn(mockEvents);

        // Creating the CustomUserDetails and authentication object
        CustomUserDetails userDetails = new CustomUserDetails(mockUser);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // Perform the GET request with the mocked authentication
        mockMvc.perform(get("/api/calendar/events/by-date")
                        .param("infoId", infoId.toString())
                        .param("date", "2025-07-24")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].jobName").value("pipeline-test"));
    }

    @Test
    @DisplayName("요약 조회 API 응답 성공 테스트")
    void getSummaryByDate_정상응답() throws Exception {
        // Mocked summary data
        CalendarSummaryRes summary = CalendarSummaryRes.builder()
                .buildCount(3)
                .errorCount(1)
                .build();

        // Mock service behavior
        Mockito.when(calendarService.getCalendarSummaryByDate(Mockito.any(), Mockito.eq(infoId), Mockito.eq("2025-07-24")))
                .thenReturn(summary);

        // 인증 객체 설정
        CustomUserDetails userDetails = new CustomUserDetails(mockUser);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // Perform GET request
        mockMvc.perform(get("/api/calendar/summary/by-date")
                        .param("infoId", infoId.toString())
                        .param("date", "2025-07-24")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.buildCount").value(3))
                .andExpect(jsonPath("$.data.errorCount").value(1));
    }

}
