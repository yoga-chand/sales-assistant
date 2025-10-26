package com.apple.salesassistant.chat.llm;

import com.apple.salesassistant.configuration.OllamaConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class OllamaProvider implements LlmProvider {

    @Autowired
    private OllamaConfig ollamaConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String chat(List<Map<String, String>> messages) {
        Map<String, Object> body = Map.of(
                "model", ollamaConfig.model(),
                "messages", messages,
                "temperature", 0.2
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    ollamaConfig.baseUrl() + "/api/chat",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            log.info("Ollama response status: " + response.getStatusCode());
            return parseResponse(response);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to call Ollama: " + e.getMessage(), e);
        }
    }

    private String parseResponse(ResponseEntity<String> resp) throws JsonProcessingException {
        String contentType = Optional.ofNullable(resp.getHeaders().getContentType())
                .map(MediaType::toString).orElse("");

        String rawBody = resp.getBody();
        if (contentType.contains("application/x-ndjson")) {
            return parseNdjsonResponse(rawBody);
        } else {
            return parseJsonResponse(rawBody);
        }
    }

    private String parseNdjsonResponse(String rawBody) {
        StringBuilder result = new StringBuilder();
        ObjectMapper mapper = new ObjectMapper();

        try (BufferedReader reader = new BufferedReader(new StringReader(rawBody))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                var node = mapper.readTree(line);
                String content = node.path("message").path("content").asText(null);
                if (content != null) result.append(content);

                if (node.path("done").asBoolean(false)) break;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error parsing NDJSON response", e);
        }
        return result.toString();
    }

    private String parseJsonResponse(String rawBody) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        var root = mapper.readTree(rawBody);

        String content = root.path("message").path("content").asText(null);
        if (content != null) return content;

        var messages = root.path("messages");
        if (messages.isArray() && messages.size() > 0) {
            return messages.get(messages.size() - 1).path("content").asText(null);
        }

        throw new IllegalStateException("Unexpected JSON response: " + rawBody);
    }

    @Override
    public String key() {
        return "ollama";
    }
}
