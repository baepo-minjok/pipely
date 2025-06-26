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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;

@Slf4j
@Service
@RequiredArgsConstructor
public class JenkinsInfoService {

    private final RestTemplate restTemplate;
    private final JenkinsInfoRepository jenkinsInfoRepository;

    /**
     * 새로운 JenkinsInfo 생성
     */
    @Transactional
    public JenkinsInfo createJenkinsInfo(Users user, CreateDto createDto) {

        JenkinsInfo info = JenkinsInfo.builder()
                .id(UUID.randomUUID())
                .name(createDto.getName())
                .description(createDto.getDescription())
                .jenkinsId(createDto.getJenkinsId())
                .secretKey(createDto.getSecretKey()) // 실제로는 암호화/안전 저장 고려
                .uri(createDto.getUri())
                .user(user)
                .build();

        JenkinsInfo saved = jenkinsInfoRepository.save(info);

        user.getJenkinsInfos().add(saved);

        return saved;
    }

    /**
     * JenkinsInfo 수정 (예: URI나 secretKey 업데이트)
     */
    @Transactional
    public JenkinsInfo updateJenkinsInfo(UpdateDto updateDto) {
        JenkinsInfo info = jenkinsInfoRepository.findById(updateDto.getInfoId())
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));

        info.setName(updateDto.getName());
        info.setDescription(updateDto.getDescription());
        info.setSecretKey(updateDto.getSecretKey());
        info.setUri(updateDto.getUri());
        info.setJenkinsId(updateDto.getJenkinsId());

        return jenkinsInfoRepository.save(info);
    }

    /**
     * JenkinsInfo 삭제
     *
     * @param infoId JenkinsInfo 엔티티의 private key
     */
    @Transactional
    public void deleteJenkinsInfo(UUID infoId) {
        JenkinsInfo info = jenkinsInfoRepository.findById(infoId)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));
        jenkinsInfoRepository.delete(info);
    }

    /**
     * 특정 사용자의 모든 JenkinsInfo 조회
     */
    public List<LightInfoDto> getAllLightDtoByUser(Users user) {
        return jenkinsInfoRepository.findByUser(user).stream()
                .map(LightInfoDto::fromEntity)
                .toList();
    }

    /**
     * id로 하나의 JenkinsInfo 조회
     *
     * @param infoId JenkinsInfo 엔티티의 private key
     * @return 조회된 JenkinsInfo를 DetailInfoDto로 변환해서 반환, 없으면 JENKINS_INFO_NOT_FOUND 에러 반환
     */
    public DetailInfoDto getDetailInfoById(UUID infoId) {
        return jenkinsInfoRepository.findById(infoId).map(DetailInfoDto::fromEntity)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));
    }

    public String verificationJenkinsInfo(UUID infoId) {
        JenkinsInfo jenkinsInfo = jenkinsInfoRepository.findById(infoId)
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_INFO_NOT_FOUND));

        String baseUri = jenkinsInfo.getUri();
        String username = jenkinsInfo.getJenkinsId();
        String apiToken = jenkinsInfo.getSecretKey();

        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "xml", StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        // 검사할 엔드포인트: /api/json
        String endpoint = baseUri;
        // URI 끝에 슬래시 유무 확인 후 "/api/json"을 붙임
        if (baseUri.endsWith("/")) {
            endpoint = baseUri + "api/json";
        } else {
            endpoint = baseUri + "/api/json";
        }
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
        } catch (CancellationException ex) {
            log.warn(ex.getMessage());
            throw new CustomException(ErrorCode.JENKINS_URI_NOT_FOUND);
        }
        HttpStatusCode httpStatusCode = response.getStatusCode();
        log.info(httpStatusCode.toString());
        if (httpStatusCode == HttpStatus.OK) {
            return response.getBody();
        } else if (httpStatusCode == HttpStatus.NOT_FOUND) {
            throw new CustomException(ErrorCode.JENKINS_ENDPOINT_NOT_FOUND);
        } else if (httpStatusCode == HttpStatus.UNAUTHORIZED) {
            throw new CustomException(ErrorCode.JENKINS_AUTHENTICATION_FAILED);
        } else if (httpStatusCode == HttpStatus.GATEWAY_TIMEOUT || httpStatusCode == HttpStatus.REQUEST_TIMEOUT) {
            throw new CustomException(ErrorCode.JENKINS_CONNECTION_TIMEOUT_OR_NETWORK_ERROR);
        } else if (httpStatusCode.is5xxServerError()) {
            throw new CustomException(ErrorCode.JENKINS_SERVER_ERROR);
        } else {
            throw new CustomException(ErrorCode.JENKINS_CONNECTION_FAILED);
        }
    }
}