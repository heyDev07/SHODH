package com.shodh.contest.config;

import com.shodh.contest.service.DockerExecutionService;
import com.shodh.contest.service.LocalExecutionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ExecutionServiceConfig {

    @Value("${execution.service.type:docker}")
    private String executionServiceType;

    @Bean
    @Primary
    public Object executionService() {
        if ("local".equals(executionServiceType)) {
            return new LocalExecutionService();
        } else {
            return new DockerExecutionService();
        }
    }
}
