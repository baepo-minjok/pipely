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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JenkinsInfoService {

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
     */
    @Transactional
    public void deleteJenkinsInfo(UUID infoId) {
        JenkinsInfo info = jenkinsInfoRepository.findById(infoId)
                .orElseThrow(() -> new IllegalArgumentException("Jenkins 정보를 찾을 수 없습니다: " + infoId));
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
}