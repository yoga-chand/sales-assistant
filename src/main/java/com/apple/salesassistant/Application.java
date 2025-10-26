package com.apple.salesassistant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@Slf4j
@ConfigurationPropertiesScan("com.apple.salesassistant.configuration")
public class Application {
    public static void main(String[] args) {
        log.info("Starting Apple Sales Assistant Application...");
        SpringApplication.run(Application.class, args);
    }
}
