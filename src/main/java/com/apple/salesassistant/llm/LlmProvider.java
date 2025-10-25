package com.apple.salesassistant.llm;

import java.util.List;
import java.util.Map;

public interface LlmProvider {

    /**
     * Execute a chat completion and return assistant text.
     * @param model          provider-specific model name/id
     * @param systemPrompt   optional system instructions (nullable)
     * @param messages       user/assistant turns, last item is typically the user prompt
     * @return assistant text
     */
    String chat(List<Map<String, String>> messages);

    /**
     * Unique provider key, e.g. "ollama" or "bedrock".
     */
    String key();
}