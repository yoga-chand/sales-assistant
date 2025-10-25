package com.apple.salesassistant.chat.service;

import com.apple.salesassistant.chat.kb.KbRetriever;
import com.apple.salesassistant.chat.kb.KnowledgeBase;
import com.apple.salesassistant.chat.llm.LlmProvider;
import com.apple.salesassistant.chat.llm.LlmProviderSelector;
import com.apple.salesassistant.chat.llm.SystemPromptLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ChatService {
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
        return llmProvider.chat(messages);

    }

}


