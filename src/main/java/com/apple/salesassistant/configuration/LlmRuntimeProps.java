package com.apple.salesassistant.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm")
public record LlmRuntimeProps(
        String provider,     // "ollama" | "bedrock" | ...
        String systemPrompt         // optional default system prompt
) {}

