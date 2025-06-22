package com.example.backend.jenkins.info.service;

import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.repository.UserRepository;
import com.example.backend.jenkins.info.model.JenkinsInfo;
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
    private final UserRepository userRepository;

    /**
     * 새로운 JenkinsInfo 생성
     */
    @Transactional
    public JenkinsInfo createJenkinsInfo(String userId, String jenkinsId, String secretKey, String uri) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));


        JenkinsInfo info = JenkinsInfo.builder()
                // id는 UUIDGenerator가 자동 할당
                .jenkinsId(jenkinsId)
                .secretKey(secretKey) // 실제로는 암호화/안전 저장 고려
                .uri(uri)
                .user(user)
                .build();

        JenkinsInfo saved = jenkinsInfoRepository.save(info);

        // 양방향 매핑을 사용 중이라면:
        // user.getJenkinsInfos().add(saved);

        return saved;
    }

    /**
     * 특정 사용자의 모든 JenkinsInfo 조회
     */
    @Transactional(readOnly = true)
    public List<JenkinsInfo> getAllByUser(String userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        return jenkinsInfoRepository.findByUser(user);
    }

    /**
     * JenkinsInfo 수정 (예: URI나 secretKey 업데이트)
     */
    @Transactional
    public JenkinsInfo updateJenkinsInfo(UUID infoId, String newSecretKey, String newUri) {
        JenkinsInfo info = jenkinsInfoRepository.findById(infoId)
                .orElseThrow(() -> new IllegalArgumentException("Jenkins 정보를 찾을 수 없습니다: " + infoId));
        // 필요한 검증: 권한 체크(현재 사용자와 소유자 일치 등)
        info.setSecretKey(newSecretKey);
        info.setUri(newUri);
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
}