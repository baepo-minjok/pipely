package com.example.backend.jenkins.job.controller;

import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.service.CustomUserDetails;
import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import com.example.backend.jenkins.job.model.dto.ResponseDto;
import com.example.backend.jenkins.job.service.PipelineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PipelineService pipelineService;

    @MockitoBean
    private JenkinsInfoService jenkinsInfoService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AuthenticationManager authManager;


    @Test
    @DisplayName("Job 생성 - 성공")
    void createJob_success() throws Exception {

        // Request 받기
        RequestDto.CreateDto dto = RequestDto.CreateDto.builder()
                .infoId(UUID.randomUUID())
                .scriptId(UUID.randomUUID())
                .name("sample-job")
                .description("테스트 job입니다.")
                .trigger(true)
                .schedule("매일 오후 3시 5분")
                .build();

        // http 호출 x
        doNothing().when(pipelineService).createJob(any());

        // 현재 expression= "userEntity"를 쓰고 있으므로 수동으로  Authentication, CustomUserDetails 생성
        Authentication auth = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        // create에서는 getname이 필요하므로 name 지정
        Users mockUser = Users.builder().id(UUID.randomUUID()).name("testuser").build();

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUserEntity()).thenReturn(mockUser);

        mockMvc.perform(post("/api/jenkins/job/create")
                        .with(request -> {
                            SecurityContext context = SecurityContextHolder.createEmptyContext();
                            context.setAuthentication(auth);  // 위에서 만든 auth 사용
                            SecurityContextHolder.setContext(context);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                // data에 response 출력
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("create job success"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("Job 수정 - 성공")
    void updateJob_success() throws Exception {
        RequestDto.UpdateDto dto = RequestDto.UpdateDto.builder()
                .pipelineId(UUID.randomUUID())
                .scriptId(UUID.randomUUID())
                .name("updated-job")
                .description("업데이트된 job입니다.")
                .trigger(false)
                .schedule("매주 월요일 오전 10시 30분")
                .build();

        // http 호출 x
        doNothing().when(pipelineService).updateJob(any());

        // 현재 expression= "userEntity"를 쓰고 있으므로 수동으로  Authentication, CustomUserDetails 생성
        Authentication auth = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        // create에서는 getname이 필요하므로 name 지정
        Users mockUser = Users.builder().id(UUID.randomUUID()).name("testuser").build();

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUserEntity()).thenReturn(mockUser);


        mockMvc.perform(put("/api/jenkins/job")
                        .with(request -> {
                            SecurityContext context = SecurityContextHolder.createEmptyContext();
                            context.setAuthentication(auth);  // 위에서 만든 auth 사용
                            SecurityContextHolder.setContext(context);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("update job success"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("Job soft-delete - 성공")
    void softDelete_success() throws Exception {
        UUID jobId = UUID.randomUUID();
        doNothing().when(pipelineService).softDeletePipelineById(jobId);

        mockMvc.perform(delete("/api/jenkins/job")
                        .param("jobId", jobId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("soft-delete job success"))
                .andExpect(jsonPath("$.error").doesNotExist());

    }

    @Test
    @DisplayName("Job hard-delete - 성공")
    void hardDelete_success() throws Exception {
        UUID jobId = UUID.randomUUID();
        doNothing().when(pipelineService).hardDeletePipelineById(jobId);

        mockMvc.perform(delete("/api/jenkins/job/hard")
                        .param("jobId", jobId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("hard-delete job success"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("Job 목록 조회 - 성공")
    void getAll_success() throws Exception {
        UUID jenkinsInfoId = UUID.randomUUID();
        when(pipelineService.getLightJobs(jenkinsInfoId))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/jenkins/job")
                        .param("jenkinsInfoId", jenkinsInfoId.toString()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("Job 상세 조회 - 성공")
    void getDetail_success() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(pipelineService.getDetailJob(jobId))
                .thenReturn(ResponseDto.DetailJobDto.builder()
                        .pipelineId(jobId)
                        .name("test")
                        .build());

        mockMvc.perform(get("/api/jenkins/job/detail")
                        .param("jobId", jobId.toString()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pipelineId").value(jobId.toString()))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("삭제된 Job 목록 조회 - 성공")
    void getDeletedList_success() throws Exception {
        UUID jenkinsInfoId = UUID.randomUUID();
        when(pipelineService.getDeletedLightJobs(jenkinsInfoId))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/jenkins/job/deleted")
                        .param("jenkinsInfoId", jenkinsInfoId.toString()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("삭제된 Job 복구 - 성공")
    void restorationJob_success() throws Exception {
        UUID jobId = UUID.randomUUID();
        doNothing().when(pipelineService).restorationJob(jobId);

        mockMvc.perform(get("/api/jenkins/job/restoration")
                        .param("jobId", jobId.toString()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("restoration job success"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }
}
