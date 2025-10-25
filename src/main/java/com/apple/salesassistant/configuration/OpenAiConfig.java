package com.apple.salesassistant.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
public record OpenAiConfig(
        String apiKey,                 // required
        String baseUrl,                // default https://api.openai.com

        String model,                  // e.g., gpt-4, gpt-3.5-turbo
        int timeoutSeconds             // e.g., 60
) {}

