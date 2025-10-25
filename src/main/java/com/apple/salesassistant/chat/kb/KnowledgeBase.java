package com.apple.salesassistant.chat.kb;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

@Component
public class KnowledgeBase {
    private final String text;

    public KnowledgeBase(ResourceLoader loader, org.springframework.core.env.Environment env) throws Exception {
        var path = env.getProperty("kb.file-path", "classpath:kb/kb.txt");
        var r = loader.getResource(path);
        this.text = new String(r.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public String text() { return text; }
}
