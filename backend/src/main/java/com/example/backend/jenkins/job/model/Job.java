package com.example.backend.jenkins.job.model;

import com.example.backend.jenkins.info.model.JenkinsInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    // git webhook trigger 설정 여부
    private Boolean trigger;

    // Build Stage가 선택되었는지 여부
    private Boolean isBuildSelected;

    // Test Stage가 선택되었는지 여부
    private Boolean isTestSelected;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jenkins_info_id", nullable = false)
    private JenkinsInfo jenkinsInfo;
}
