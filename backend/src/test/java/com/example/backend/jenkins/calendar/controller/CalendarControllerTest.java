package com.example.backend.jenkins.calendar.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.jenkins.calendar.service.CalendarService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

@WebMvcTest(CalendarController.class)
@AutoConfigureMockMvc(addFilters = false)
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private CalendarService calendarService;

    private UUID infoId;
    private Users mockUser;

    @BeforeEach
    void setUp() {
        infoId = UUID.randomUUID();
        mockUser = Users.builder().id(UUID.randomUUID()).build();
    }
}
