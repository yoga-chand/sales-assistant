package com.apple.salesassistant.chat.service;

import com.apple.salesassistant.auth.api.filter.JwtAuthFilter;
import com.apple.salesassistant.auth.dto.UserContext;
import com.apple.salesassistant.chat.kb.InMemoryKb;
import com.apple.salesassistant.chat.kb.KbChunk;
import com.apple.salesassistant.chat.kb.KbPolicy;
import com.apple.salesassistant.chat.kb.KbRetriever;
import com.apple.salesassistant.chat.llm.LlmService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ChatService {

  private final InMemoryKb kb;
  private final KbRetriever retriever;
  private final LlmService llm; // your provider-switching service

  public ChatService(InMemoryKb kb, KbRetriever retriever, LlmService llm) {
    this.kb = kb;
    this.retriever = retriever;
    this.llm = llm;
  }

  /** Main entry from controller */
  public Map<String, Object> answer(String userQuestion) {
    UserContext userContext = getUserContext();


    // Candidate KB → ABAC filter → topK selection
    List<KbChunk> candidates = kb.all();
    List<KbChunk> allowed = candidates.stream()
        .filter(c -> KbPolicy.canSee(userContext, c))
        .toList();

    List<KbChunk> ctx = retriever.topK(userQuestion, allowed, userContext);

    // Build role banner (non-authoritative, for assistant style only)
    String roleBanner = banner(userContext.roles().stream().toList());
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

  private static UserContext getUserContext() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    UserContext userContext;
    if (auth == null || !auth.isAuthenticated()
            || auth instanceof AnonymousAuthenticationToken
            || !(auth.getPrincipal() instanceof JwtAuthFilter.AuthUser u)) {
      // Guest fallback
      userContext = new UserContext(
              "guest",
              "t1",
              Set.of("ROLE_GUEST"),
              Set.of(),
              Set.of() // e.g., allowedTags based on public policy
      );
    } else {
      var principal = (JwtAuthFilter.AuthUser)auth.getPrincipal();
      userContext = new UserContext(
              principal.id(),
              /* tenantId */ "t1",
              Set.copyOf(principal.roles()),
              Set.copyOf(principal.scopes()),
              /* allowedTags */ Set.of());
    }
    return userContext;
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
