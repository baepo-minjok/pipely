package com.example.backend.jenkins.job.model;

import com.example.backend.jenkins.info.model.JenkinsInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
public class Pipeline {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    // job 이름
    private String name;

    // 삭제 여부
    private Boolean isDeleted;

    // 생성 시간
    private LocalDateTime createdAt;

    // 수정 시간
    private LocalDateTime updatedAt;

    // 삭제 시간
    private LocalDateTime deletedAt;

    @Column(name = "latest_version")
    private Integer latestVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jenkins_info_id", nullable = false)
    private JenkinsInfo jenkinsInfo;

    @Builder.Default
    @OneToMany(mappedBy = "pipeline", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PipelineVersion> versionList = new ArrayList<>();

}

