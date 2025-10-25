package com.apple.salesassistant.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ollama")
public record OllamaConfig(
        String baseUrl,
        String model,
        int timeoutSeconds
) {}
