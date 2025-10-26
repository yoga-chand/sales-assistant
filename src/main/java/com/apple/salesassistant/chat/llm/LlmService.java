package com.apple.salesassistant.chat.llm;

import com.apple.salesassistant.chat.kb.KbRetriever;
import com.apple.salesassistant.chat.kb.KnowledgeBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class LlmService {
    @Autowired
    private LlmProviderSelector llmProviderSelector;

    @Autowired
    private KnowledgeBase knowledgeBase;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SystemPromptLoader systemPromptLoader;

    @Autowired
    private KbRetriever kbRetriever;

    @SuppressWarnings("unchecked")
    public String chat(String userMessage) {

        String systemPrompt = systemPromptLoader.load();

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        );

        LlmProvider llmProvider = llmProviderSelector.select(Optional.empty());
        log.info("Using LLM provider: %s".formatted(llmProvider.key()));
        return llmProvider.chat(messages);

    }

}


