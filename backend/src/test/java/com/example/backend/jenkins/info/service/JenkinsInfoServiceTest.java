package com.example.backend.jenkins.info.service;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.model.dto.InfoRequestDto.CreateDto;
import com.example.backend.jenkins.info.model.dto.InfoRequestDto.UpdateDto;
import com.example.backend.jenkins.info.model.dto.InfoResponseDto.DetailInfoDto;
import com.example.backend.jenkins.info.model.dto.InfoResponseDto.LightInfoDto;
import com.example.backend.jenkins.info.repository.JenkinsInfoRepository;
import com.example.backend.service.HttpClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JenkinsInfoServiceTest {

    @Mock
    private HttpClientService httpClientService;

    @Mock
    private JenkinsInfoRepository jenkinsInfoRepository;

    @InjectMocks
    private JenkinsInfoService jenkinsInfoService;

    private Users user;
    private JenkinsInfo existingInfo;
    private UUID infoId;

    @BeforeEach
    void setUp() {
        user = new Users();
        infoId = UUID.randomUUID();
        existingInfo = JenkinsInfo.builder()
                .id(infoId)
                .name("Name")
                .description("Desc")
                .jenkinsId("admin")
                .apiToken("token")
                .uri("http://jenkins")
                .user(user)
                .build();
    }

    @Test
    @DisplayName("createJenkinsInfo: 새 정보 저장 및 리턴")
    void createJenkinsInfo_success() {
        CreateDto dto = CreateDto.builder()
                .name("Name")
                .description("Desc")
                .jenkinsId("admin")
                .apiToken("token")
                .uri("http://jenkins")
                .build();
        when(jenkinsInfoRepository.save(any(JenkinsInfo.class))).thenAnswer(inv -> inv.getArgument(0));

        JenkinsInfo result = jenkinsInfoService.createJenkinsInfo(user, dto);

        assertNotNull(result);
        assertEquals("Name", result.getName());
        verify(jenkinsInfoRepository).save(any(JenkinsInfo.class));
        assertTrue(user.getJenkinsInfoList().contains(result));
    }

    @Test
    @DisplayName("updateJenkinsInfo: 존재하지 않는 경우 예외")
    void updateJenkinsInfo_notFound() {
        UpdateDto dto = UpdateDto.builder().infoId(infoId).build();
        when(jenkinsInfoRepository.findById(infoId)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> jenkinsInfoService.updateJenkinsInfo(dto));
        assertEquals(ErrorCode.JENKINS_INFO_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("updateJenkinsInfo: 수정 후 저장 반환")
    void updateJenkinsInfo_success() {
        UpdateDto dto = UpdateDto.builder()
                .infoId(infoId)
                .name("New")
                .description("New Desc")
                .jenkinsId("user")
                .apiToken("newToken")
                .uri("http://new")
                .build();
        when(jenkinsInfoRepository.findById(infoId)).thenReturn(Optional.of(existingInfo));
        when(jenkinsInfoRepository.save(existingInfo)).thenReturn(existingInfo);

        JenkinsInfo updated = jenkinsInfoService.updateJenkinsInfo(dto);

        assertEquals("New", updated.getName());
        verify(jenkinsInfoRepository).save(existingInfo);
    }

    @Test
    @DisplayName("deleteJenkinsInfo: 존재하지 않으면 예외")
    void deleteJenkinsInfo_notFound() {
        when(jenkinsInfoRepository.findById(infoId)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> jenkinsInfoService.deleteJenkinsInfo(infoId));
        assertEquals(ErrorCode.JENKINS_INFO_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("deleteJenkinsInfo: 성공 시 delete 호출")
    void deleteJenkinsInfo_success() {
        when(jenkinsInfoRepository.findById(infoId)).thenReturn(Optional.of(existingInfo));

        jenkinsInfoService.deleteJenkinsInfo(infoId);

        verify(jenkinsInfoRepository).delete(existingInfo);
    }

    @Test
    @DisplayName("getAllLightDtoByUser: 리스트 반환")
    void getAllLightDtoByUser() {
        when(jenkinsInfoRepository.findByUser(user)).thenReturn(List.of(existingInfo));

        List<LightInfoDto> list = jenkinsInfoService.getAllLightDtoByUser(user);

        assertEquals(1, list.size());
        assertEquals(infoId, list.get(0).getId());
    }

    @Test
    @DisplayName("getDetailInfoById: 존재하지 않으면 예외")
    void getDetailInfoById_notFound() {
        when(jenkinsInfoRepository.findById(infoId)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> jenkinsInfoService.getDetailInfoById(infoId));
        assertEquals(ErrorCode.JENKINS_INFO_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("getDetailInfoById: 성공 시 DTO 반환")
    void getDetailInfoById_success() {
        when(jenkinsInfoRepository.findById(infoId)).thenReturn(Optional.of(existingInfo));

        DetailInfoDto dto = jenkinsInfoService.getDetailInfoById(infoId);

        assertEquals(infoId, dto.getId());
    }

    @Test
    @DisplayName("verificationJenkinsInfo: 존재하지 않으면 예외")
    void verificationJenkinsInfo_notFound() {
        when(jenkinsInfoRepository.findById(infoId)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> jenkinsInfoService.verificationJenkinsInfo(infoId));
        assertEquals(ErrorCode.JENKINS_INFO_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("verificationJenkinsInfo: 올바른 엔드포인트 호출")
    void verificationJenkinsInfo_success() {
        when(jenkinsInfoRepository.findById(infoId)).thenReturn(Optional.of(existingInfo));

        jenkinsInfoService.verificationJenkinsInfo(infoId);

        verify(httpClientService).exchange(
                startsWith("http://jenkins"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("getJenkinsInfo: 존재하지 않으면 예외")
    void getJenkinsInfo_notFound() {
        when(jenkinsInfoRepository.findWithUserById(infoId)).thenReturn(Optional.empty());

        assertThrows(CustomException.class,
                () -> jenkinsInfoService.getJenkinsInfo(infoId));
    }

    @Test
    @DisplayName("getJenkinsInfo: 존재 시 리턴")
    void getJenkinsInfo_success() {
        when(jenkinsInfoRepository.findWithUserById(infoId)).thenReturn(Optional.of(existingInfo));

        JenkinsInfo info = jenkinsInfoService.getJenkinsInfo(infoId);

        assertEquals(existingInfo, info);
    }
}
