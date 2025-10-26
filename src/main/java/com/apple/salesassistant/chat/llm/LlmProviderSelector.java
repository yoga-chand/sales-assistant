package com.apple.salesassistant.chat.llm;

import com.apple.salesassistant.configuration.LlmRuntimeProps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LlmProviderSelector {

    private final Map<String, LlmProvider> registry;
    private final String defaultKey;

    public LlmProviderSelector(List<LlmProvider> providers, LlmRuntimeProps props) {
        this.registry = providers.stream().collect(Collectors.toUnmodifiableMap(
                LlmProvider::key, Function.identity()
        ));
        this.defaultKey = props.provider();
    }

    public LlmProvider select(Optional<String> overrideKey) {
        String key = overrideKey.filter(k -> registry.containsKey(k)).orElse(defaultKey);
        LlmProvider p = registry.get(key);
        if (p == null) throw new IllegalArgumentException("Unknown LLM provider: " + key);
        log.info("Selected LLM provider: %s".formatted(p.key()));
        return p;
    }
}
