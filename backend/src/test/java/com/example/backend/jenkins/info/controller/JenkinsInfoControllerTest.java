package com.example.backend.jenkins.info.controller;

import com.example.backend.config.jwt.JwtTokenProvider;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.dto.InfoRequestDto.CreateDto;
import com.example.backend.jenkins.info.model.dto.InfoRequestDto.InfoDto;
import com.example.backend.jenkins.info.model.dto.InfoRequestDto.UpdateDto;
import com.example.backend.jenkins.info.model.dto.InfoResponseDto.DetailInfoDto;
import com.example.backend.jenkins.info.service.JenkinsInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JenkinsInfoController.class)
@AutoConfigureMockMvc(addFilters = false)
public class JenkinsInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JenkinsInfoService jenkinsInfoService;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    static Stream<Arguments> verificationExceptionProvider() {
        return Stream.of(
                Arguments.of(
                        ErrorCode.JENKINS_AUTHENTICATION_FAILED,
                        HttpStatus.NOT_FOUND,
                        "JENKINS_AUTHENTICATION_FAILED_404"
                ),
                Arguments.of(
                        ErrorCode.JENKINS_ENDPOINT_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "JENKINS_ENDPOINT_NOT_FOUND_404"
                ),
                Arguments.of(
                        ErrorCode.JENKINS_URI_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "JENKINS_URI_NOT_FOUND_404"
                ),
                Arguments.of(
                        ErrorCode.JENKINS_CONNECTION_FAILED,
                        HttpStatus.NOT_FOUND,
                        "JENKINS_CONNECTION_FAILED_404"
                ),
                Arguments.of(
                        ErrorCode.JENKINS_SERVER_ERROR,
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "JENKINS_SERVER_ERROR_500"
                ),
                Arguments.of(
                        ErrorCode.JENKINS_CONNECTION_TIMEOUT_OR_NETWORK_ERROR,
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "JENKINS_CONNECTION_TIMEOUT_OR_NETWORK_ERROR_500"
                )

        );
    }

    @Test
    @DisplayName("POST /api/jenkins/info/create - 성공 시 200 반환 및 서비스 호출")
    void createInfo_success() throws Exception {
        CreateDto dto = CreateDto.builder()
                .uri("http://jenkins.example.com")
                .name("my jenkins")
                .description("회사 ci 서버입니다.")
                .jenkinsId("admin")
                .apiToken("token123")
                .build();

        mockMvc.perform(post("/api/jenkins/info/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("create jenkins info success"))
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(jenkinsInfoService).createJenkinsInfo(any(), eq(dto));
    }

    @Test
    @DisplayName("POST /api/jenkins/info/create - 필수값 누락시 400 반환")
    void createInfo_valid() throws Exception {
        CreateDto dto = CreateDto.builder()
                .name("my jenkins")
                .description("회사 ci 서버입니다.")
                .jenkinsId("admin")
                .apiToken("33333")
                .build();

        mockMvc.perform(post("/api/jenkins/info/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED_400"))
                .andExpect(jsonPath("$.error.message").exists());
    }

    @Test
    @DisplayName("PUT /api/jenkins/info - 성공 시 200 반환 및 서비스 호출")
    void updateInfo_success() throws Exception {
        UpdateDto dto = UpdateDto.builder()
                .infoId(UUID.randomUUID())
                .uri("http://jenkins.example.com")
                .name("not my jenkins")
                .description("회사 ci 서버 아닙니다.")
                .jenkinsId("admin")
                .apiToken("token1243")
                .build();

        mockMvc.perform(put("/api/jenkins/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Jenkins Info update Success"))
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(jenkinsInfoService).updateJenkinsInfo(eq(dto));
    }

    @Test
    @DisplayName("PUT /api/jenkins/info - 없는 jenkins info를 수정하면 404 반환")
    void updateInfo_not_found() throws Exception {
        UpdateDto dto = UpdateDto.builder()
                .infoId(UUID.randomUUID())
                .uri("http://jenkins.example.com")
                .name("not my jenkins")
                .description("회사 ci 서버 아닙니다.")
                .jenkinsId("admin")
                .apiToken("token1243")
                .build();

        when(jenkinsInfoService.updateJenkinsInfo(eq(dto)))
                .thenThrow(new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));

        mockMvc.perform(put("/api/jenkins/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("JENKINS_INFO_NOT_FOUND_404"))
                .andExpect(jsonPath("$.error.message").exists());
    }

    @Test
    @DisplayName("PUT /api/jenkins/info - 필수값 누락시 400 반환")
    void updateInfo_valid() throws Exception {
        UpdateDto dto = UpdateDto.builder()
                .infoId(UUID.randomUUID())
                .name("not my jenkins")
                .description("회사 ci 서버 아닙니다.")
                .jenkinsId("admin")
                .apiToken("token1243")
                .build();

        mockMvc.perform(put("/api/jenkins/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED_400"))
                .andExpect(jsonPath("$.error.message").exists());
    }

    @Test
    @DisplayName("GET /api/jenkins/info - 리스트 조회 시 200 반환 및 서비스 호출")
    void getAllInfo_success() throws Exception {
        when(jenkinsInfoService.getAllLightDtoByUser(any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/jenkins/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        verify(jenkinsInfoService).getAllLightDtoByUser(any());
    }

    @Test
    @DisplayName("POST /api/jenkins/info - 상세 조회 시 200 반환 및 서비스 호출")
    void getDetailInfo_success() throws Exception {
        UUID id = UUID.randomUUID();
        InfoDto dto = InfoDto.builder().infoId(id).build();
        DetailInfoDto detail = DetailInfoDto.builder()
                .id(UUID.randomUUID())
                .name("my jenkins")
                .description("description")
                .jenkinsId("admin")
                .apiToken("token123")
                .uri("http://jenkins.example.com")
                .build();
        when(jenkinsInfoService.getDetailInfoById(id)).thenReturn(detail);

        mockMvc.perform(post("/api/jenkins/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(detail.getId().toString()))
                .andExpect(jsonPath("$.data.name").value("my jenkins"))
                .andExpect(jsonPath("$.data.description").value("description"))
                .andExpect(jsonPath("$.data.jenkinsId").value("admin"))
                .andExpect(jsonPath("$.data.apiToken").value("token123"))
                .andExpect(jsonPath("$.data.uri").value("http://jenkins.example.com"))
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(jenkinsInfoService).getDetailInfoById(id);
    }

    @Test
    @DisplayName("POST /api/jenkins/info - 없는 ID로 상세 조회 시 404 반환")
    void getDetailInfo_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        InfoDto dto = InfoDto.builder().infoId(id).build();

        when(jenkinsInfoService.getDetailInfoById(id))
                .thenThrow(new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));

        mockMvc.perform(post("/api/jenkins/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("JENKINS_INFO_NOT_FOUND_404"))
                .andExpect(jsonPath("$.error.message").exists());

        verify(jenkinsInfoService).getDetailInfoById(id);
    }

    @Test
    @DisplayName("POST /api/jenkins/info - 상세 조회 시 ID 누락으로 400 반환")
    void getDetailInfo_validationFail() throws Exception {
        InfoDto dto = InfoDto.builder().build();

        mockMvc.perform(post("/api/jenkins/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED_400"))
                .andExpect(jsonPath("$.error.message").exists());
    }

    @Test
    @DisplayName("DELETE /api/jenkins/info/{infoId} - 삭제 시 200 반환 및 서비스 호출")
    void deleteInfo_success() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/jenkins/info/{infoId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("delete Jenkins Info Success"))
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(jenkinsInfoService).deleteJenkinsInfo(id);
    }

    @Test
    @DisplayName("DELETE /api/jenkins/info/{infoId} - 없는 ID 삭제 시 404 반환")
    void deleteInfo_notFound() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND))
                .when(jenkinsInfoService).deleteJenkinsInfo(id);

        mockMvc.perform(delete("/api/jenkins/info/{infoId}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("JENKINS_INFO_NOT_FOUND_404"))
                .andExpect(jsonPath("$.error.message").exists());

        verify(jenkinsInfoService).deleteJenkinsInfo(id);
    }

    @Test
    @DisplayName("POST /api/jenkins/info/verification - 검증 시 200 반환 및 서비스 호출")
    void verifyInfo_success() throws Exception {
        UUID id = UUID.randomUUID();
        InfoDto dto = InfoDto.builder().infoId(id).build();

        mockMvc.perform(post("/api/jenkins/info/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("verify success"))
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(jenkinsInfoService).verificationJenkinsInfo(id);
    }

    @Test
    @DisplayName("POST /api/jenkins/info/verification - 검증 실패 시 404 반환")
    void verifyInfo_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        InfoDto dto = InfoDto.builder().infoId(id).build();

        doThrow(new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND))
                .when(jenkinsInfoService).verificationJenkinsInfo(id);

        mockMvc.perform(post("/api/jenkins/info/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("JENKINS_INFO_NOT_FOUND_404"))
                .andExpect(jsonPath("$.error.message").exists());

        verify(jenkinsInfoService).verificationJenkinsInfo(id);
    }

    @ParameterizedTest(name = "[{index}] {0} 예외일 때 {1} 반환, code={2}")
    @MethodSource("verificationExceptionProvider")
    void verifyInfo_errorResponses(
            ErrorCode errorCode,
            HttpStatus expectedStatus,
            String expectedJsonCode
    ) throws Exception {

        UUID id = UUID.randomUUID();
        InfoDto dto = InfoDto.builder().infoId(id).build();
        doThrow(new CustomException(errorCode))
                .when(jenkinsInfoService)
                .verificationJenkinsInfo(id);

        mockMvc.perform(post("/api/jenkins/info/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(expectedJsonCode))
                .andExpect(jsonPath("$.error.message").exists());

        verify(jenkinsInfoService).verificationJenkinsInfo(id);
    }
}
