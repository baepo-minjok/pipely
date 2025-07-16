package com.example.backend.jenkins.job.model.dto;

import com.example.backend.jenkins.job.model.Pipeline;
import com.example.backend.jenkins.job.model.PipelineVersion;
import com.example.backend.jenkins.job.model.Script;
import com.example.backend.jenkins.job.model.Stage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ResponseDto {

    public static LightJobDto entityToLightJobDto(Pipeline pipeline) {
        PipelineVersion pipelineVersion = pipeline.getVersionList().get(pipeline.getLatestVersion() - 1);

        return LightJobDto.builder()
                .pipelineId(pipeline.getId())
                .name(pipeline.getName())
                .description(pipelineVersion.getDescription())
                .build();
    }

    public static DetailJobDto entityToDetailJobDto(Pipeline pipeline) {
        PipelineVersion pipelineVersion = pipeline.getVersionList().get(pipeline.getLatestVersion() - 1);
        Script script = pipelineVersion.getScript();

        return DetailJobDto.builder()
                .pipelineId(pipeline.getId())
                .name(pipeline.getName())
                .description(pipelineVersion.getDescription())
                .trigger(pipelineVersion.getIsTriggered())
                .createdAt(pipeline.getCreatedAt())
                .updatedAt(pipeline.getUpdatedAt())
                .deletedAt(pipeline.getDeletedAt())
                .lightScriptDto(entityToLightScriptDto(script))
                .latestVersion(pipeline.getLatestVersion())
                .stageList(toListOfStageDtos(pipelineVersion.getStageList()))
                .isSuccessfulBuild(pipelineVersion.getIsSuccessfulBuild())
                .schedule(pipelineVersion.getSchedule())
                .pipelineVersionList(toListOfPipelineVersionDtos(pipeline.getVersionList()))
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
                .springProfile(script.getSpringProfile())
                .replicas(script.getReplicas())
                .script(script.getScript())
                .build();
    }

    public static List<PipelineVersionDto> toListOfPipelineVersionDtos(List<PipelineVersion> pipelines) {
        return pipelines.stream().map(ResponseDto::entityToPipelineVersionDto).toList();
    }

    public static PipelineVersionDto entityToPipelineVersionDto(PipelineVersion version) {
        return PipelineVersionDto.builder()
                .version(version.getVersion())
                .createdAt(version.getCreatedAt())
                .isSuccessfulBuild(version.getIsSuccessfulBuild())
                .build();
    }

    public static List<StageDto> toListOfStageDtos(List<Stage> stageList) {
        return stageList.stream().map(ResponseDto::entityToStageDto).toList();
    }

    public static StageDto entityToStageDto(Stage stage) {
        return StageDto.builder()
                .stageName(stage.getName())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LightJobDto {

        private UUID pipelineId;

        private String name;
        
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailJobDto {

        LightScriptDto lightScriptDto;
        private UUID pipelineId;
        // job 이름
        private String name;
        // job 설명
        private String description;
        // git webhook trigger 설정 여부
        private Boolean trigger;
        // 생성 시간
        private LocalDateTime createdAt;
        // 수정 시간
        private LocalDateTime updatedAt;
        // 삭제 시간
        private LocalDateTime deletedAt;
        // 최신 버전
        private Integer latestVersion;

        private Boolean isSuccessfulBuild;

        private String schedule;

        private List<StageDto> stageList;

        private List<PipelineVersionDto> pipelineVersionList;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LightScriptDto {

        private UUID scriptId;
        // 연동된 git 주소
        private String githubUrl;

        // clone할 branch 이름
        private String branch;

        // Build Stage가 선택되었는지 여부
        private Boolean isBuildSelected;

        // Test Stage가 선택되었는지 여부
        private Boolean isTestSelected;

        private Boolean isK8sDeploy;
        private String tag;
        private String sshKeyPath;
        private String sshPort;
        private String deployTarget;
        private String k8sPath;
        private String deploymentName;
        private String namespace;
        private String appName;
        private String containerName;
        private String imageRepo;
        private String port;
        private String springProfile;
        private String replicas;

        private String script;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PipelineVersionDto {
        private Integer version;
        private LocalDateTime createdAt;
        private Boolean isSuccessfulBuild;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StageDto {

        private String stageName;

    }
}
