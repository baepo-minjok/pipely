package com.example.backend.jenkins.job.model.dto;

import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.notification.model.dto.NotificationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class RequestDto {

    public static Pipeline toEntity(CreateDto requestDto, JenkinsInfo info) {

        return Pipeline.builder()
                .name(requestDto.getName())
                .jenkinsInfo(info)
                .createdAt(LocalDateTime.now())
                .buildState(Pipeline.BuildState.BUILD_INIT)
                .isDeleted(false)
                .build();
    }

    @SuperBuilder //Test에서 생성하기 위함
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static abstract class BaseDto {

        @Schema(
                description = "수정에 사용할 Script 고유 식별자 (UUID)",
                example = "0c6fd9ad-991c-4e62-abe7-723b4be4a57e",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private UUID scriptId;

        @NotBlank
        @Size(max = 100)
        @Schema(
                description = "수정할 Jenkins Job 이름",
                example = "updated-job",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String name;

        @Size(max = 255)
        @Schema(
                description = "Job 설명",
                example = "수정된 Job 설명",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String description;

        @NotNull
        @Schema(
                description = "Git webhook 트리거 사용 여부",
                example = "false",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private Boolean trigger;

        @Size(max = 100)
        @Pattern(
                regexp = "^$|^(매일|매주)\\s*(?:([월화수목금토일](?:,\\s*[월화수목금토일])*)(?:요일)?)?\\s*(오전\\s*\\d{1,2}시\\s*\\d{1,2}분?|오후\\s*\\d{1,2}시\\s*\\d{1,2}분?|\\d{1,2}:\\d{1,2})$",
                message = "형식 예시: '매일 오후 3시 5분', '매주 월,수,금 오전 9시 30분' 등으로 입력해야 합니다."
        )
        @Schema(
                description = "스케줄(cron) 설정 값",
                example = "매주 월,수,금 오전 9시 30분",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String schedule;

        private Map<String, NotificationDto> notificationMap;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "ScriptBaseDto", description = "Jenkins Script 생성/수정에 필요한 파라미터")
    public static class ScriptBaseDto {
        @Schema(
                description = "Script 고유 식별자 (UUID)",
                example = "0c6fd9ad-991c-4e62-abe7-723b4be4a57e",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private UUID scriptId;

        @NotBlank
        @Size(max = 200)
        @Schema(
                description = "연동된 git 저장소 주소",
                example = "https://github.com/org/repo.git",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String githubUrl;

        @NotBlank
        @Size(max = 50)
        @Schema(
                description = "빌드 시 clone할 git 브랜치명",
                example = "main",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String branch;

        @NotNull
        @Schema(
                description = "빌드 스테이지 선택 여부",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private Boolean isBuildSelected;

        @NotNull
        @Schema(
                description = "테스트 스테이지 선택 여부",
                example = "false",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private Boolean isTestSelected;

        @Schema(
                description = "Kubernetes 배포 여부",
                example = "true",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private Boolean isK8sDeploy;

        @Size(max = 50)
        @Schema(
                description = "이미지 태그 (ex: latest, 1.0.0)",
                example = "latest",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String tag;

        @Size(max = 200)
        @Schema(
                description = "원격 서버에 복사할 yaml 파일 경로",
                example = "/home/ubuntu/app/deploy.yaml",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String k8sPath;

        @Size(max = 100)
        @Schema(
                description = "Kubernetes Deployment 명",
                example = "my-app-deployment",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String deploymentName;

        @Size(max = 100)
        @Schema(
                description = "Kubernetes 네임스페이스",
                example = "default",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String namespace;

        @Size(max = 100)
        @Schema(
                description = "app 라벨명 (K8s용)",
                example = "my-app",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String appName;

        @Size(max = 100)
        @Schema(
                description = "컨테이너 이름",
                example = "my-container",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String containerName;

        @Size(max = 200)
        @Schema(
                description = "이미지 저장소 경로",
                example = "ghcr.io/org/project",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String imageRepo;

        @Size(max = 10)
        @Schema(
                description = "컨테이너 내부 포트",
                example = "8080",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String port;

        @Size(max = 10)
        @Schema(
                description = "디플로이먼트 복제 수",
                example = "2",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String replicas;

        @Schema(
                description = "EC2 배포 여부",
                example = "false",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private Boolean isEc2Deploy;

        @Size(max = 200)
        @Schema(
                description = "EC2 내 jar 저장 경로",
                example = "/home/ec2-user/app",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String ec2DeployPath;

        @Size(max = 200)
        @Schema(
                description = "SSH 키 경로 (ex: ~/.ssh/id_rsa)",
                example = "~/.ssh/id_rsa",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String sshKeyPath;

        @Size(max = 10)
        @Schema(
                description = "SSH 포트 (기본: 22)",
                example = "22",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String sshPort;

        @Size(max = 100)
        @Schema(
                description = "배포 대상 서버 (ex: user@ip)",
                example = "ubuntu@1.2.3.4",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String deployTarget;

    }

    @SuperBuilder
    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "JobCreateDto", description = "Jenkins Job 생성 요청 DTO")
    public static class CreateDto extends BaseDto {

        @NotNull
        @Schema(
                description = "Jenkins 서버 정보의 UUID",
                example = "2c1edbe1-4e6a-420d-84cd-3ffb2b9d7c85",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private UUID infoId;
    }

    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "JobUpdateDto", description = "Jenkins Job 수정 요청 DTO")
    public static class UpdateDto extends BaseDto {

        @NotNull
        @Schema(
                description = "수정 대상 Jenkins Job의 고유 UUID",
                example = "b5a7c7b2-8123-4cce-80ec-ccf79d5e2f7a",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private UUID pipelineId;
    }

    @Data
    @Schema(name = "ScriptValidateDto", description = "Jenkins Script 유효성 검증 요청 DTO")
    public static class ScriptValidateDto {

        @NotNull
        @Schema(
                description = "Jenkins 서버 정보의 UUID",
                example = "2c1edbe1-4e6a-420d-84cd-3ffb2b9d7c85",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private UUID infoId;

        @NotBlank
        @Schema(
                description = "유효성 검증할 Jenkins Script",
                example = "Pipeline{ ... }",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String script;
    }

    @Data
    public static class StatusDto {
        @Schema(
                description = "job의 ID",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        )
        private UUID jobId;

        private boolean success;
    }

}
