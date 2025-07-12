package com.example.backend.jenkins.build;

import com.example.backend.jenkins.build.service.BuildService;
import com.example.backend.jenkins.job.service.PipelineService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 테스트에서 사용할 Mock 객체를 등록
@Configuration
public class BuildControllerTestConfig {

    @Bean
    public BuildService buildService() {
        return Mockito.mock(BuildService.class);
    }

    @Bean
    public PipelineService pipelineService() {
        return Mockito.mock(PipelineService.class);
    }

}
