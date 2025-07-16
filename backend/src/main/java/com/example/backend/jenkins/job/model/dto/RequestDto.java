package com.example.backend.jenkins.job.model.dto;

import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.model.Pipeline;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

public class RequestDto {

    public static CreateDto toCreateDto(UpdateDto requestDto, UUID infoId) {
        return CreateDto.builder()
                .name(requestDto.getName())
                .infoId(infoId)
                .scriptId(requestDto.getScriptId())
                .description(requestDto.getDescription())
                .trigger(requestDto.getTrigger())
                .schedule(requestDto.getSchedule())
                .build();
    }

    public static Pipeline toEntity(CreateDto requestDto, JenkinsInfo info) {

        return Pipeline.builder()
                .name(requestDto.getName())
                .jenkinsInfo(info)
                .createdAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
    }

    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "ScriptBaseDto", description = "Jenkins Script 생성/수정에 필요한 파라미터")
    public static class ScriptBaseDto {

        @Schema(description = "Script 고유 식별자 (UUID)", example = "0c6fd9ad-991c-4e62-abe7-723b4be4a57e")
        private UUID scriptId;

        @Schema(description = "연동된 git 저장소 주소", example = "https://github.com/org/repo.git")
        private String githubUrl;

        @Schema(description = "빌드 시 clone할 git 브랜치명", example = "main", defaultValue = "main")
        private String branch;

        @Schema(description = "빌드 스테이지 선택 여부", example = "true")
        private Boolean isBuildSelected;

        @Schema(description = "테스트 스테이지 선택 여부", example = "false")
        private Boolean isTestSelected;

        @Schema(description = "Kubernetes 배포 여부", example = "true")
        private Boolean isK8sDeploy;

        @Schema(description = "이미지 태그 (ex: latest, 1.0.0)", example = "latest")
        private String tag;

        @Schema(description = "SSH 키 경로 (ex: ~/.ssh/id_rsa)", example = "~/.ssh/id_rsa")
        private String sshKeyPath;

        @Schema(description = "SSH 포트 (기본: 22)", example = "22")
        private String sshPort;

        @Schema(description = "배포 대상 서버 (ex: user@ip)", example = "ubuntu@1.2.3.4")
        private String deployTarget;

        @Schema(description = "원격 서버에 복사할 yaml 파일 경로", example = "/home/ubuntu/app/deploy.yaml")
        private String k8sPath;

        @Schema(description = "Kubernetes Deployment 명", example = "my-app-deployment")
        private String deploymentName;

        @Schema(description = "Kubernetes 네임스페이스", example = "default")
        private String namespace;

        @Schema(description = "app 라벨명 (K8s용)", example = "my-app")
        private String appName;

        @Schema(description = "컨테이너 이름", example = "my-container")
        private String containerName;

        @Schema(description = "이미지 저장소 경로", example = "ghcr.io/org/project")
        private String imageRepo;

        @Schema(description = "컨테이너 내부 포트", example = "8080")
        private String port;

        @Schema(description = "디플로이먼트 복제 수", example = "2")
        private String replicas;

        @Schema(description = "EC2 배포 여부", example = "false")
        private Boolean isEc2Deploy;

        @Schema(description = "EC2 내 jar 저장 경로", example = "/home/ec2-user/app")
        private String ec2DeployPath;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "CreateDto", description = "Jenkins Job 생성 요청 DTO")
    public static class CreateDto {

        @Schema(description = "Jenkins 서버 정보의 UUID", example = "2c1edbe1-4e6a-420d-84cd-3ffb2b9d7c85")
        private UUID infoId;

        @Schema(description = "Script 고유 식별자 (UUID)", example = "0c6fd9ad-991c-4e62-abe7-723b4be4a57e")
        private UUID scriptId;

        @Schema(description = "생성할 Jenkins Job 이름", example = "sample-job")
        private String name;

        @Schema(description = "Job 설명", example = "테스트 Job")
        private String description;

        @Schema(description = "Git webhook 트리거 사용 여부", example = "true")
        private Boolean trigger;

        @Schema(description = "스케줄(cron) 설정 값", example = "매일 오후 12시 30분")
        private String schedule;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "UpdateDto", description = "Jenkins Job 수정 요청 DTO")
    public static class UpdateDto {

        @Schema(description = "수정 대상 Jenkins Job의 고유 UUID", example = "b5a7c7b2-8123-4cce-80ec-ccf79d5e2f7a")
        private UUID pipelineId;

        @Schema(description = "수정에 사용할 Script 고유 식별자 (UUID)", example = "0c6fd9ad-991c-4e62-abe7-723b4be4a57e")
        private UUID scriptId;

        @Schema(description = "수정할 Jenkins Job 이름", example = "updated-job")
        private String name;

        @Schema(description = "Job 설명", example = "수정된 Job 설명")
        private String description;

        @Schema(description = "Git webhook 트리거 사용 여부", example = "false")
        private Boolean trigger;

        @Schema(description = "스케줄(cron) 설정 값", example = "매일 오후 12시 30분")
        private String schedule;
    }

    @Data
    @Schema(name = "ScriptValidateDto", description = "Jenkins Script 유효성 검증 요청 DTO")
    public static class ScriptValidateDto {

        @Schema(description = "Jenkins 서버 정보의 UUID", example = "2c1edbe1-4e6a-420d-84cd-3ffb2b9d7c85")
        private UUID infoId;

        @Schema(description = "유효성 검증할 Jenkins Script", example = "Pipeline{ ... }")
        private String script;
    }
}
