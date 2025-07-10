package com.example.backend.jenkins.job.model;

import com.example.backend.jenkins.info.model.JenkinsInfo;
import com.example.backend.jenkins.job.model.dto.RequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pipeline_job",
        uniqueConstraints = @UniqueConstraint(name = "uq_pipeline_job", columnNames = {"jenkins_info_id", "job_name"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pipeline {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    // job 이름
    private String name;

    // job 설명
    private String description;

    // git webhook trigger 설정 여부
    private Boolean isTriggered;

    // 스케줄 설정
    private String schedule;

    // 삭제 여부
    private Boolean isDeleted;

    // 생성 시간
    private LocalDateTime createdAt;

    // 수정 시간
    private LocalDateTime updatedAt;

    // 삭제 시간
    private LocalDateTime deletedAt;

    @Lob
    private String config;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "script_id", nullable = false)
    private Script script;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jenkins_info_id", nullable = false)
    private JenkinsInfo jenkinsInfo;

    /*@OneToMany(mappedBy = "pipeline", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PipelineHistory> historyList = new ArrayList<>();*/

    public static Pipeline updatePipeline(Pipeline pipeline, RequestDto.UpdateDto requestDto, String config) {
        pipeline.setDescription(requestDto.getDescription());
        pipeline.setIsTriggered(requestDto.getTrigger());
        pipeline.setUpdatedAt(LocalDateTime.now());
        pipeline.setConfig(config);

        return pipeline;
    }
}

