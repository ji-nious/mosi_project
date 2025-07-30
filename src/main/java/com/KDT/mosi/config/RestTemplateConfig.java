package com.KDT.mosi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // 기본 RestTemplate - Spring Boot가 자동으로 JSON 컨버터를 등록합니다
        return new RestTemplate();
    }
} 