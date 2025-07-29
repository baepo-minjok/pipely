package com.example.backend.jenkins.calendar.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.service.CustomUserDetails;
import com.example.backend.config.jwt.JwtAuthenticationFilter;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto;
import com.example.backend.jenkins.calendar.service.CalendarService;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarEventRes;
import com.example.backend.jenkins.calendar.model.dto.CalendarResponseDto.CalendarSummaryRes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    @DisplayName("월 단위 이벤트 조회 API 응답 성공 테스트")
    void getEventsByMonth_정상응답() throws Exception {
        // given: 테스트용 그룹 응답 데이터 생성
        List<CalendarResponseDto.CalendarEventGroupRes> mockGroups = List.of(
                CalendarResponseDto.CalendarEventGroupRes.builder()
                        .date("2025-07-24")
                        .events(List.of(
                                CalendarEventRes.builder()
                                        .type("BUILD")
                                        .jobName("pipeline-test")
                                        .buildNumber(42)
                                        .start("2025-07-24 13:20:00")
                                        .build()
                        ))
                        .build()
        );

        Mockito.when(calendarService.getEventsByMonth(Mockito.any(), Mockito.eq(infoId), Mockito.eq(2025), Mockito.eq(7)))
                .thenReturn(mockGroups);

        CustomUserDetails userDetails = new CustomUserDetails(mockUser);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // when & then: GET 요청 및 응답 검증
        mockMvc.perform(get("/api/calendar/events/by-month")
                        .param("infoId", infoId.toString())
                        .param("year", "2025")
                        .param("month", "7")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].date").value("2025-07-24"))
                .andExpect(jsonPath("$.data[0].events[0].jobName").value("pipeline-test"))
                .andExpect(jsonPath("$.data[0].events[0].type").value("BUILD"));
    }

}
