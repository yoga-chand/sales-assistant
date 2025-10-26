package com.apple.salesassistant.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
public record OpenAiConfig(
        String apiKey,
        String baseUrl,

        String model,
        int timeoutSeconds
) {}

