package com.apple.salesassistant.chat.llm;

import com.apple.salesassistant.configuration.OpenAiConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Component
public class OpenAiProvider implements  LlmProvider {

    private final OpenAiConfig openAiConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String chat(List<Map<String, String>> messages) {
        // Implementation for OpenAI chat completion

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiConfig.apiKey());

        Map<String, Object> body = Map.of(
                "model", openAiConfig.model(),
                "messages", messages,
                "temperature", 0.2
        );

        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                    normalize(openAiConfig.baseUrl()) + "/v1/chat/completions",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );
            Map<String, Object> m = resp.getBody();
            if (m == null) throw new IllegalStateException("Empty OpenAI response");
            log.info("OpenAI api response successful");
            Object choices = m.get("choices");
            if (choices instanceof List<?> list && !list.isEmpty()) {
                Object first = list.get(0);
                if (first instanceof Map<?,?> c) {
                    Object msg = c.get("message");
                    if (msg instanceof Map<?,?> mm) {
                        Object content = mm.get("content");
                        if (content != null) return content.toString();
                    }
                }
            }
            throw new IllegalStateException("Unexpected OpenAI response shape: " + m.keySet());
        } catch (RestClientException ex) {
            throw new RuntimeException("OPENAI_CALL_FAILED: " + ex.getMessage(), ex);
        }
    }

    private static String normalize(String base) {
        if (base == null || base.isBlank()) return "https://api.openai.com";
        return base.endsWith("/") ? base.substring(0, base.length()-1) : base;
    }

    @Override
    public String key() {
        return "openai";
    }
}
