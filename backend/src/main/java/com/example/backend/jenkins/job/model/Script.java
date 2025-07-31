package com.example.backend.jenkins.job.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Schema(
        name = "Script",
        description = "Jenkins Pipeline 실행에 필요한 스크립트와 배포 설정 등 전반적인 빌드/배포 파라미터를 포함하는 엔티티"
)
public class Script {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(description = "Script 고유 식별자 (UUID)", example = "0c6fd9ad-991c-4e62-abe7-723b4be4a57e")
    private UUID id;

    @Column(name = "github_url", length = 200, nullable = false)
    @Schema(description = "연동된 git 저장소 주소", example = "https://github.com/org/repo.git")
    private String githubUrl;

    @Column(name = "branch", length = 50, nullable = false)
    @Schema(description = "빌드 시 clone할 git 브랜치명", example = "main", defaultValue = "main")
    private String branch;

    @Column(name = "is_build_selected")
    @Schema(description = "빌드 스테이지 선택 여부", example = "true")
    private Boolean isBuildSelected;

    @Column(name = "is_test_selected")
    @Schema(description = "테스트 스테이지 선택 여부", example = "false")
    private Boolean isTestSelected;

    @Column(name = "is_k8s_deploy")
    @Schema(description = "Kubernetes 배포 여부", example = "true")
    private Boolean isK8sDeploy;

    @Column(name = "tag", length = 50)
    @Schema(description = "이미지 태그 (ex: latest, 1.0.0)", example = "latest")
    private String tag;

    @Column(name = "ssh_key_path", length = 200)
    @Schema(description = "SSH 키 경로 (ex: ~/.ssh/id_rsa)", example = "~/.ssh/id_rsa")
    private String sshKeyPath;

    @Column(name = "ssh_port", length = 10)
    @Schema(description = "SSH 포트 (기본: 22)", example = "22")
    private String sshPort;

    @Column(name = "deploy_target", length = 100)
    @Schema(description = "배포 대상 서버 (ex: user@ip)", example = "ubuntu@1.2.3.4")
    private String deployTarget;

    @Column(name = "k8s_path", length = 200)
    @Schema(description = "원격 서버에 복사할 yaml 파일 경로", example = "/home/ubuntu/app/deploy.yaml")
    private String k8sPath;

    @Column(name = "deployment_name", length = 100)
    @Schema(description = "Kubernetes Deployment 명", example = "my-app-deployment")
    private String deploymentName;

    @Column(name = "namespace", length = 100)
    @Schema(description = "Kubernetes 네임스페이스", example = "default")
    private String namespace;

    @Column(name = "app_name", length = 100)
    @Schema(description = "app 라벨명 (K8s용)", example = "my-app")
    private String appName;

    @Column(name = "container_name", length = 100)
    @Schema(description = "컨테이너 이름", example = "my-container")
    private String containerName;

    @Column(name = "image_repo", length = 200)
    @Schema(description = "이미지 저장소 경로", example = "ghcr.io/org/project")
    private String imageRepo;

    @Column(name = "port", length = 10)
    @Schema(description = "컨테이너 내부 포트", example = "8080")
    private String port;

    @Column(name = "replicas", length = 10)
    @Schema(description = "디플로이먼트 복제 수", example = "2")
    private String replicas;

    @Column(name = "is_ec2_deploy")
    @Schema(description = "EC2 배포 여부", example = "false")
    private Boolean isEc2Deploy;

    @Column(name = "ec2_deploy_path", length = 200)
    @Schema(description = "EC2 내 jar 저장 경로", example = "/home/ec2-user/app")
    private String ec2DeployPath;

    @Lob
    @Schema(description = "실행할 스크립트(셸, 파이프라인 등)", example = "#!/bin/bash\necho Hello World")
    private String script;

    @Builder.Default
    @OneToMany(mappedBy = "script", orphanRemoval = true)
    @Schema(
            description = "해당 스크립트로 생성된 PipelineVersion 리스트 (내부 참조용)",
            implementation = com.example.backend.jenkins.job.model.PipelineVersion.class,
            hidden = true // API 응답에서 숨김
    )
    private List<PipelineVersion> pipelineVersionList = new ArrayList<>();

    public static Script toEntity(com.example.backend.jenkins.job.model.dto.RequestDto.ScriptBaseDto requestDto, String script) {
        return Script.builder()
                .githubUrl(requestDto.getGithubUrl())
                .branch(requestDto.getBranch())
                .isTestSelected(requestDto.getIsTestSelected())
                .isBuildSelected(requestDto.getIsBuildSelected())
                .script(script)
                .build();
    }

    public static Script replicateEntity(Script entity) {
        return Script.builder()
                .githubUrl(entity.getGithubUrl())
                .branch(entity.getBranch())
                .isBuildSelected(entity.getIsBuildSelected())
                .isTestSelected(entity.getIsTestSelected())
                .isK8sDeploy(entity.getIsK8sDeploy())
                .tag(entity.getTag())
                .sshKeyPath(entity.getSshKeyPath())
                .sshPort(entity.getSshPort())
                .deployTarget(entity.getDeployTarget())
                .k8sPath(entity.getK8sPath())
                .deploymentName(entity.getDeploymentName())
                .namespace(entity.getNamespace())
                .appName(entity.getAppName())
                .containerName(entity.getContainerName())
                .imageRepo(entity.getImageRepo())
                .port(entity.getPort())
                .replicas(entity.getReplicas())
                .isEc2Deploy(entity.getIsEc2Deploy())
                .ec2DeployPath(entity.getEc2DeployPath())
                .script(entity.getScript())
                .pipelineVersionList(new ArrayList<>())
                .build();
    }

}

