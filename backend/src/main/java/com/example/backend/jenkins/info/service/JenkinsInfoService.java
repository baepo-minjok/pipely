package com.example.backend.jenkins.info.service;

import com.example.backend.auth.user.model.Users;
import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.info.model.dto.InfoRequestDto;
import com.example.backend.jenkins.info.model.dto.InfoRequestDto.CreateDto;
import com.example.backend.jenkins.info.model.dto.InfoRequestDto.UpdateDto;
import com.example.backend.jenkins.info.model.dto.InfoResponseDto.DetailInfoDto;
import com.example.backend.jenkins.info.model.dto.InfoResponseDto.LightInfoDto;
import com.example.backend.jenkins.info.repository.JenkinsInfoRepository;
import com.example.backend.service.HttpClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JenkinsInfoService {

    private final HttpClientService httpClientService;
    private final JenkinsInfoRepository jenkinsInfoRepository;

    /**
     * Create a new JenkinsInfo entity for a given user.
     *
     * @param user      the user who owns the JenkinsInfo
     * @param createDto DTO containing Jenkins connection details
     * @return the saved JenkinsInfo entity
     */
    @Transactional
    public JenkinsInfo createJenkinsInfo(Users user, CreateDto createDto) {

        JenkinsInfo info = JenkinsInfo.builder()
                .name(createDto.getName())
                .description(createDto.getDescription())
                .jenkinsId(createDto.getJenkinsId())
                .apiToken(createDto.getApiToken())
                .uri(createDto.getUri())
                .connected(createDto.isConnected())
                .user(user)
                .build();

        JenkinsInfo saved = jenkinsInfoRepository.save(info);

        user.getJenkinsInfoList().add(saved);

        return saved;
    }

    /**
     * Update an existing JenkinsInfo entity.
     * Example: update URI, description, or API token.
     *
     * @param updateDto DTO containing updated JenkinsInfo details
     * @return the updated JenkinsInfo entity
     * @throws CustomException if the JenkinsInfo with given ID is not found
     */
    @Transactional
    public JenkinsInfo updateJenkinsInfo(UpdateDto updateDto) {
        JenkinsInfo info = jenkinsInfoRepository.findById(updateDto.getInfoId())
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));

        info.setName(updateDto.getName());
        info.setDescription(updateDto.getDescription());
        info.setUri(updateDto.getUri());
        info.setConnected(false);
        info.setJenkinsId(updateDto.getJenkinsId());

        if (updateDto.getApiToken() != null && !updateDto.getApiToken().isEmpty()) {
            info.setApiToken(updateDto.getApiToken());
        }

        return jenkinsInfoRepository.save(info);
    }

    /**
     * Delete a JenkinsInfo by ID.
     *
     * @param infoId the unique ID (primary key) of the JenkinsInfo entity
     * @throws CustomException if the JenkinsInfo with given ID is not found
     */
    @Transactional
    public void deleteJenkinsInfo(UUID infoId) {
        JenkinsInfo info = jenkinsInfoRepository.findById(infoId)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));
        jenkinsInfoRepository.delete(info);
    }

    /**
     * Retrieve all JenkinsInfo entities for a given user in lightweight form.
     *
     * @param user the user whose JenkinsInfo list should be retrieved
     * @return list of LightInfoDto objects containing basic Jenkins info
     */
    public List<LightInfoDto> getAllLightDtoByUser(Users user) {
        return jenkinsInfoRepository.findByUser(user).stream()
                .map(LightInfoDto::fromEntity)
                .toList();
    }

    /**
     * Retrieve detailed information about a JenkinsInfo by ID.
     *
     * @param infoId the unique ID of the JenkinsInfo entity
     * @return DetailInfoDto containing full information about the JenkinsInfo
     * @throws CustomException if the JenkinsInfo with given ID is not found
     */
    public DetailInfoDto getDetailInfoById(UUID infoId) {
        return jenkinsInfoRepository.findById(infoId).map(DetailInfoDto::fromEntity)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));
    }

    /**
     * Verify that stored Jenkins connection information is valid
     * by sending a request to the Jenkins API.
     *
     * @param infoId the unique ID of the JenkinsInfo entity
     * @throws CustomException if the JenkinsInfo with given ID is not found
     * @throws CustomException if Jenkins API call fails (connection or authentication error)
     */
    @Transactional
    public void verificationJenkinsInfo(UUID infoId) {
        JenkinsInfo jenkinsInfo = jenkinsInfoRepository.findById(infoId)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));

        String baseUri = jenkinsInfo.getUri();
        String username = jenkinsInfo.getJenkinsId();
        String apiToken = jenkinsInfo.getApiToken();

        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "xml", StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        String endpoint = baseUri;

        if (baseUri.endsWith("/")) {
            endpoint = baseUri + "api/json";
        } else {
            endpoint = baseUri + "/api/json";
        }
        try {
            httpClientService.exchange(
                    endpoint,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            jenkinsInfo.setConnected(true);
            jenkinsInfoRepository.save(jenkinsInfo);
        } catch (Exception e) {
            jenkinsInfo.setConnected(false);
            jenkinsInfoRepository.save(jenkinsInfo);
            throw e;
        }

    }

    /**
     * Verify Jenkins connection information provided directly via DTO
     * without persisting it to the database.
     *
     * @param dto DTO containing Jenkins URI, jenkinsId, and apiToken
     * @throws CustomException if Jenkins API call fails (connection or authentication error)
     */
    @Transactional
    public void verificationJenkinsInfo(InfoRequestDto.InfoDto dto) {

        String baseUri = dto.getUri();
        String username = dto.getJenkinsId();
        String apiToken = dto.getApiToken();

        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "xml", StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        String endpoint = baseUri;

        if (baseUri.endsWith("/")) {
            endpoint = baseUri + "api/json";
        } else {
            endpoint = baseUri + "/api/json";
        }
        httpClientService.exchange(
                endpoint,
                HttpMethod.GET,
                requestEntity,
                String.class
        );

    }

    /**
     * Retrieve a JenkinsInfo entity with its associated user.
     *
     * @param infoId the unique ID of the JenkinsInfo entity
     * @return the JenkinsInfo entity including user reference
     * @throws CustomException if the JenkinsInfo with given ID is not found
     */
    public JenkinsInfo getJenkinsInfo(UUID infoId) {
        return jenkinsInfoRepository.findWithUserById(infoId)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));
    }

    /**
     * Check if a user is the owner of a given JenkinsInfo entity.
     *
     * @param user   the user to check
     * @param infoId the unique ID of the JenkinsInfo entity
     * @return true if the user is the owner, false otherwise
     */
    public boolean isOwner(Users user, UUID infoId) {
        JenkinsInfo info = getJenkinsInfo(infoId);
        UUID userId = user.getId();
        UUID confirmUserId = info.getUser().getId();
        return userId.equals(confirmUserId);
    }
}