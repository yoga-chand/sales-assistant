package com.apple.salesassistant.chat.service;

import com.apple.salesassistant.auth.api.filter.JwtAuthFilter;
import com.apple.salesassistant.auth.dto.UserContext;
import com.apple.salesassistant.chat.kb.InMemoryKb;
import com.apple.salesassistant.chat.kb.KbChunk;
import com.apple.salesassistant.chat.kb.KbPolicy;
import com.apple.salesassistant.chat.kb.KbRetriever;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GroundedChatService {

  private final InMemoryKb kb;
  private final KbRetriever retriever;
  private final ChatService llm; // your provider-switching service

  public GroundedChatService(InMemoryKb kb, KbRetriever retriever, ChatService llm) {
    this.kb = kb;
    this.retriever = retriever;
    this.llm = llm;
  }

  /** Main entry from controller */
  public Map<String, Object> answer(String userQuestion) {
    var principal = (JwtAuthFilter.AuthUser)
        SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    // Build ABAC user context (tenant/tags are placeholders you can wire later)
    var auth = new UserContext(
        principal.id(),
        /* tenantId */ "t1",
        Set.copyOf(principal.roles()),
        Set.copyOf(principal.scopes()),
        /* allowedTags */ Set.of() // e.g., Set.of("apac","services")
    );

    // Candidate KB → ABAC filter → topK selection
    List<KbChunk> candidates = kb.all();
    List<KbChunk> allowed = candidates.stream()
        .filter(c -> KbPolicy.canSee(auth, c))
        .toList();

    List<KbChunk> ctx = retriever.topK(userQuestion, allowed, auth);

    // Build role banner (non-authoritative, for assistant style only)
    String roleBanner = banner(principal.roles());
    String prompt = buildPrompt(roleBanner, ctx, userQuestion);

    // Call the model (strategy pattern picks OpenAI or Ollama)
    String answer = llm.chat(prompt);

    return Map.of(
        "role", roleBanner,
        "answer", answer,
        "citations", ctx.stream().map(c -> Map.of(
            "chunk_id", c.chunkId().toString(),
            "title", c.title(),
            "tags", c.tags()
        )).toList()
    );
  }

  private static String banner(List<String> roles) {
    if (roles.contains("ROLE_ADMIN")) return "ROLE=ADMIN";
    if (roles.contains("ROLE_ANALYST")) return "ROLE=ANALYST";
    return "ROLE=GUEST";
  }

  private static String buildPrompt(String roleBanner, List<KbChunk> ctx, String question) {
    StringBuilder sb = new StringBuilder();
    sb.append(roleBanner).append('\n');
    sb.append("Answer STRICTLY using the CONTEXT. If data is not visible for your role, say so.\n\n");
    sb.append("CONTEXT:\n");
    for (KbChunk c : ctx) {
      sb.append("### ").append(c.title()).append(" [").append(c.chunkId()).append("]\n");
      sb.append(c.text()).append("\n\n");
    }
    sb.append("QUESTION:\n").append(question);
    return sb.toString();
  }
}
