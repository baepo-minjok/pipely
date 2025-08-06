package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ThreadPoolConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService buildPollingExecutorService() {
        return Executors.newFixedThreadPool(10);
    }
}
