package com.apple.salesassistant.chat.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SystemPromptLoader {

    private final ResourceLoader resourceLoader;
    private final org.springframework.core.env.Environment env;
    private String cachedPrompt;

    public SystemPromptLoader(ResourceLoader resourceLoader, org.springframework.core.env.Environment env) {
        this.resourceLoader = resourceLoader;
        this.env = env;
    }

    public String load() {
        if (cachedPrompt != null) return cachedPrompt;

        String location = env.getProperty("llm.system-prompt-file", "classpath:prompts/system_prompt.txt");
        try {
            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                throw new IllegalStateException("System prompt file not found: " + location);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                cachedPrompt = reader.lines().collect(Collectors.joining("\n"));
            }
            log.info("Loaded system prompt from " + location);
            return cachedPrompt;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load system prompt from " + location, e);
        }
    }
}
