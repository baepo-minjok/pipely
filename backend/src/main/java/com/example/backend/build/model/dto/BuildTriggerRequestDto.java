package com.example.backend.build.model.dto;


import com.example.backend.build.model.BuildTriggerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter



public class BuildTriggerRequestDto {



    private String jobName;

    private BuildTriggerType buildTriggerType;


}
