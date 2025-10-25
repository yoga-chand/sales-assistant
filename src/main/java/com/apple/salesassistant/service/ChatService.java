package com.apple.salesassistant.service;



//import com.apple.domain.llm.LlmProvider;
//import com.apple.domain.llm.LlmProvider.Message;
import com.apple.salesassistant.configuration.LlmRuntimeProps;
import com.apple.salesassistant.configuration.OllamaConfig;
import com.apple.salesassistant.configuration.OpenAiConfig;
import com.apple.salesassistant.kb.KnowledgeBase;
import com.apple.salesassistant.llm.LlmProvider;
import com.apple.salesassistant.llm.LlmProviderSelector;
import com.apple.salesassistant.llm.SystemPromptLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ChatService {
//    private final LlmProviderSelector selector;
//    private final LlmRuntimeProps props;
//
//    public ChatService(LlmProviderSelector selector, LlmRuntimeProps props) {
//        this.selector = selector;
//        this.props = props;
//    }

    @Autowired
    private LlmProviderSelector llmProviderSelector;

    @Autowired
    private OllamaConfig ollamaConfig;

    @Autowired
    private OpenAiConfig openAiConfig;

    @Autowired
    private KnowledgeBase knowledgeBase;

    @Autowired
    private LlmRuntimeProps llmRuntimeProps;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SystemPromptLoader systemPromptLoader;

//    public String chatWithDefaults(String userMessage) {
//
//        Map<String, Object> payload = Map.of(
//                "model", ollamaConfig.model(),
//                "messages", List.of(
//                        Map.of("role", "system", "content", "You are a helpful sales analyst. Be concise."),
//                        Map.of("role", "user", "content", userMessage)
//                ),
//                "stream", false
//        );
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
//
//        try {
//            ResponseEntity<Map> response = restTemplate.exchange(
//                    ollamaConfig.baseUrl() + "/api/chat",
//                    HttpMethod.POST,
//                    entity,
//                    Map.class
//            );
//
//            Map<String, Object> resp = response.getBody();
//            if (resp == null) throw new IllegalStateException("Empty Ollama response");
//
//            Object message = resp.get("message");
//            if (message instanceof Map<?, ?> m && m.get("content") != null)
//                return m.get("content").toString();
//
//            Object msgs = resp.get("messages");
//            if (msgs instanceof List<?> list && !list.isEmpty()) {
//                Object last = list.get(list.size() - 1);
//                if (last instanceof Map<?, ?> lm && lm.get("content") != null)
//                    return lm.get("content").toString();
//            }
//
//            throw new IllegalStateException("Unexpected Ollama response format: " + resp.keySet());
//        }
//        catch (Exception e) {
//            throw new RuntimeException("Failed to call Ollama: " + e.getMessage(), e);
//        }
//    }

    @SuppressWarnings("unchecked")
    public String chat(String userMessage) {

        String systemPrompt = systemPromptLoader.load();

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "system", "content", "CONTEXT:\n" + knowledgeBase.text()),
                Map.of("role", "user", "content", userMessage)
        );

        LlmProvider llmProvider = llmProviderSelector.select(Optional.empty());
        return llmProvider.chat(messages);

    }

    private static String normalize(String base) {
        if (base == null || base.isBlank()) return "https://api.openai.com";
        return base.endsWith("/") ? base.substring(0, base.length()-1) : base;
    }
    }


