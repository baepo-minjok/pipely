package com.example.backend.jenkins.job.model;

import com.example.backend.jenkins.info.model.JenkinsInfo;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
public class Job {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    // job 이름
    private String name;

    // job 설명
    private String description;

    // 연동된 git 주소
    private String githubUrl;

    // clone할 branch 이름
    private String branch;

    // 빌드를 실행할 폴더 경로
    private String directory;

    // git webhook trigger 설정 여부
    private Boolean trigger;

    // Build Stage가 선택되었는지 여부
    private Boolean isBuildSelected;

    // 선택한 Build 도구
    private String buildTool;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jenkins_info_id", nullable = false)
    private JenkinsInfo jenkinsInfo;
}
