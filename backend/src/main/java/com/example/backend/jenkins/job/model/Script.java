package com.example.backend.jenkins.job.model;


import com.example.backend.jenkins.job.model.dto.RequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Script {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    // 연동된 git 주소
    private String githubUrl;

    // clone할 branch 이름
    private String branch;

    // Build Stage가 선택되었는지 여부
    private Boolean isBuildSelected;

    // Test Stage가 선택되었는지 여부
    private Boolean isTestSelected;

    @Lob
    private String script;

    public static Script toEntity(RequestDto.ScriptBaseDto requestDto, String script) {
        return Script.builder()
                .githubUrl(requestDto.getGithubUrl())
                .branch(requestDto.getBranch())
                .isTestSelected(requestDto.getIsTestSelected())
                .isBuildSelected(requestDto.getIsBuildSelected())
                .script(script)
                .build();
    }
}
