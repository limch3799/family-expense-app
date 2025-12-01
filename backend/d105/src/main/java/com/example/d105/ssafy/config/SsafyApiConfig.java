package com.example.d105.ssafy.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ssafy.api")
@Data
public class SsafyApiConfig {

    @Value("${ssafy.api.base-url}")
    private String baseUrl ;
    @Value("${ssafy.api.api-key}")
    private String apiKey;
    @Value("${ssafy.api.timeout}")
    private int timeout = 30000;
    @Value("${ssafy.api.max-retry}")
    private int maxRetry = 3;
}
