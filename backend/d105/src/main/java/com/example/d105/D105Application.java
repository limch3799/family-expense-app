package com.example.d105;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class,
//        org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration.class // Function AutoConfig 제외


    })
@EnableAsync
@EnableScheduling
public class D105Application {

    public static void main(String[] args) {
        SpringApplication.run(D105Application.class, args);
    }


}