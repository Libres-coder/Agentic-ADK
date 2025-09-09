package com.alibaba.agentic.computer.use.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Pandora Boot应用的入口类
 */
@Configuration
@SpringBootApplication(scanBasePackages = {"com.alibaba.aidc", "com.alibaba.agentic"}, exclude = {})
@PropertySource("classpath:application.properties")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

