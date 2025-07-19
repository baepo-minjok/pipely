package com.example.backend.jenkins.job.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "Stage",
        description = "Jenkins Pipeline 내 Stage(단계) 정보를 저장하는 엔티티"
)
public class Stage {

    @Id
    @Column(name = "name", nullable = false, updatable = false, length = 50)
    @Schema(
            description = "Stage의 이름 (보통 대문자, 알파벳·숫자·언더바 등)",
            example = "BUILD"
    )
    private String name;

    @Builder.Default
    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VersionStage> versionList = new ArrayList<>();
}
