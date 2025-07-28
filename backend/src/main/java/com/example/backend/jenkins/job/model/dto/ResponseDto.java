package com.example.backend.jenkins.job.model.dto;

import com.example.backend.exception.CustomException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.PipelineVersion;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.VersionStage;
import com.example.backend.jenkins.notification.model.dto.NotificationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ResponseDto {

    public static LightJobDto entityToLightJobDto(Pipeline pipeline) {

        UUID latestVersionId = pipeline.getLatestVersionId();

        PipelineVersion latestVersion = pipeline.getVersionList().stream()
                .filter(v -> v.getId().equals(latestVersionId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_VERSION_NOT_FOUND));

        return LightJobDto.builder()
                .pipelineId(pipeline.getId())
                .name(pipeline.getName())
                .description(latestVersion.getDescription())
                .isBuildSuccess(pipeline.getIsBuildSuccess())
                .build();
    }

    public static DetailJobDto entityToDetailJobDto(Pipeline pipeline) {

        UUID latestVersionId = pipeline.getLatestVersionId();

        PipelineVersion latestVersion = pipeline.getVersionList().stream()
                .filter(v -> v.getId().equals(latestVersionId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.JENKINS_JOB_VERSION_NOT_FOUND));

        Script script = latestVersion.getScript();

        Map<String, NotificationDto> notificationDtos = NotificationDto.toNotificationMap(latestVersion.getJobNotificationList());

        return DetailJobDto.builder()
                .pipelineId(pipeline.getId())
                .name(pipeline.getName())
                .description(latestVersion.getDescription())
                .trigger(latestVersion.getIsTriggered())
                .createdAt(pipeline.getCreatedAt())
                .updatedAt(pipeline.getUpdatedAt())
                .deletedAt(pipeline.getDeletedAt())
                .lightScriptDto(entityToLightScriptDto(script))
                .stageList(toListOfStageDtos(latestVersion.getStageList()))
                .isBuildSuccess(pipeline.getIsBuildSuccess())
                .schedule(latestVersion.getSchedule())
                .pipelineVersionList(toListOfPipelineVersionDtos(pipeline.getVersionList()))
                .notificationList(notificationDtos)
                .build();
    }

    public static LightScriptDto entityToLightScriptDto(Script script) {
        if (script == null) {
            return null;
        }
        return LightScriptDto.builder()
                .scriptId(script.getId())
                .githubUrl(script.getGithubUrl())
                .branch(script.getBranch())
                .isBuildSelected(script.getIsBuildSelected())
                .isTestSelected(script.getIsTestSelected())
                .isK8sDeploy(script.getIsK8sDeploy())
                .tag(script.getTag())
                .sshKeyPath(script.getSshKeyPath())
                .sshPort(script.getSshPort())
                .deployTarget(script.getDeployTarget())
                .k8sPath(script.getK8sPath())
                .deploymentName(script.getDeploymentName())
                .namespace(script.getNamespace())
                .appName(script.getAppName())
                .containerName(script.getContainerName())
                .imageRepo(script.getImageRepo())
                .port(script.getPort())
                .replicas(script.getReplicas())
                .script(script.getScript())
                .build();
    }

    public static List<PipelineVersionDto> toListOfPipelineVersionDtos(List<PipelineVersion> pipelines) {
        return pipelines.stream().map(ResponseDto::entityToPipelineVersionDto).toList();
    }

    public static PipelineVersionDto entityToPipelineVersionDto(PipelineVersion version) {
        return PipelineVersionDto.builder()
                .versionId(version.getId())
                .name(version.getName())
                .createdAt(version.getCreatedAt())
                .build();
    }

    public static List<StageDto> toListOfStageDtos(List<VersionStage> stageList) {
        return stageList.stream().map(ResponseDto::entityToStageDto).toList();
    }

    public static StageDto entityToStageDto(VersionStage vs) {
        return StageDto.builder()
                .stageName(vs.getStage().getName())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "LightJobDto", description = "Jenkins Job 요약 정보")
    public static class LightJobDto {

        @Schema(description = "Job(Pipeline) UUID", example = "b1a7c7b2-8123-4cce-80ec-ccf79d5e2f7a")
        private UUID pipelineId;

        @Schema(description = "Job 이름", example = "sample-job")
        private String name;

        @Schema(description = "Job 설명", example = "테스트 Job")
        private String description;

        private Boolean isBuildSuccess;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "DetailJobDto", description = "Jenkins Job 상세 정보")
    public static class DetailJobDto {

        @Schema(description = "연결된 Script 정보")
        private LightScriptDto lightScriptDto;

        @Schema(description = "Job(Pipeline) UUID", example = "b1a7c7b2-8123-4cce-80ec-ccf79d5e2f7a")
        private UUID pipelineId;

        @Schema(description = "Job 이름", example = "sample-job")
        private String name;

        @Schema(description = "Job 설명", example = "테스트 Job")
        private String description;

        @Schema(description = "Git webhook 트리거 사용 여부", example = "true")
        private Boolean trigger;

        @Schema(description = "생성 시간 (ISO 8601)", example = "2024-07-16T15:32:10")
        private LocalDateTime createdAt;

        @Schema(description = "수정 시간 (ISO 8601)", example = "2024-07-16T15:45:00")
        private LocalDateTime updatedAt;

        @Schema(description = "삭제 시간 (ISO 8601)", example = "2024-07-16T16:00:00")
        private LocalDateTime deletedAt;

        @Schema(description = "최근 빌드 성공 여부 (true: 성공, false: 실패, null: 빌드 전)", example = "true")
        private Boolean isBuildSuccess;

        @Schema(description = "빌드/테스트/배포 등 스케줄(cron)", example = "매일 오후 12시 30분")
        private String schedule;

        @Schema(description = "이 Job의 Stage 리스트")
        private List<StageDto> stageList;

        @Schema(description = "파이프라인의 전체 버전 기록 리스트")
        private List<PipelineVersionDto> pipelineVersionList;

        @Schema(description = "현재 Job의 알림 설정 목록")
        private Map<String, NotificationDto> notificationList;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "LightScriptDto", description = "Script 주요 정보 (Job 상세 응답에 포함)")
    public static class LightScriptDto {

        @Schema(description = "Script UUID", example = "0c6fd9ad-991c-4e62-abe7-723b4be4a57e")
        private UUID scriptId;

        @Schema(description = "연동 git 저장소 주소", example = "https://github.com/org/repo.git")
        private String githubUrl;

        @Schema(description = "빌드 시 clone할 브랜치명", example = "main")
        private String branch;

        @Schema(description = "빌드 스테이지 선택 여부", example = "true")
        private Boolean isBuildSelected;

        @Schema(description = "테스트 스테이지 선택 여부", example = "false")
        private Boolean isTestSelected;

        @Schema(description = "Kubernetes 배포 여부", example = "true")
        private Boolean isK8sDeploy;

        @Schema(description = "이미지 태그", example = "latest")
        private String tag;

        @Schema(description = "SSH 키 경로", example = "~/.ssh/id_rsa")
        private String sshKeyPath;

        @Schema(description = "SSH 포트", example = "22")
        private String sshPort;

        @Schema(description = "배포 대상 서버", example = "ubuntu@1.2.3.4")
        private String deployTarget;

        @Schema(description = "k8s yaml 경로", example = "/home/ubuntu/app/deploy.yaml")
        private String k8sPath;

        @Schema(description = "K8s Deployment 명", example = "my-app-deployment")
        private String deploymentName;

        @Schema(description = "K8s 네임스페이스", example = "default")
        private String namespace;

        @Schema(description = "app 라벨명", example = "my-app")
        private String appName;

        @Schema(description = "컨테이너 이름", example = "my-container")
        private String containerName;

        @Schema(description = "이미지 저장소", example = "ghcr.io/org/project")
        private String imageRepo;

        @Schema(description = "컨테이너 포트", example = "8080")
        private String port;

        @Schema(description = "replica 개수", example = "2")
        private String replicas;

        @Schema(description = "실행 스크립트", example = "#!/bin/bash\necho Hello World")
        private String script;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(name = "PipelineVersionDto", description = "파이프라인의 각 버전별 정보")
    public static class PipelineVersionDto {


        @Schema(description = "Pipeline Version ID", example = "0c6fd9ad-991c-4e62-abe7-723b4be4a57e")
        private UUID versionId;

        @Schema(description = "파이프라인 버전 번호", example = "버전1")
        private String name;

        @Schema(description = "버전 생성 일시 (ISO 8601)", example = "2024-07-16T15:32:10")
        private LocalDateTime createdAt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(name = "StageDto", description = "Job의 Stage 정보")
    public static class StageDto {

        @Schema(description = "Stage 이름 (영문, 대문자 등)", example = "BUILD")
        private String stageName;
    }

}
