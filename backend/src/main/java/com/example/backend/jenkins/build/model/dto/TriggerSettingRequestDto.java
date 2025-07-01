package com.example.backend.jenkins.build.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TriggerSettingRequestDto {
    private String jobName;
    private List<String> steps;
}