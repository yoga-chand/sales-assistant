package com.apple.salesassistant.chat.kb;

import com.apple.salesassistant.auth.dto.UserContext;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class KbRetriever {

  private final int topK;

  public KbRetriever(org.springframework.core.env.Environment env) {
    this.topK = Integer.parseInt(env.getProperty("kb.top-k","4"));
  }

  public List<KbChunk> topK(String query, List<KbChunk> all, UserContext user) {
    // ABAC filter first
    var pool = all.stream().filter(c -> KbPolicy.canSee(user, c)).toList();
    var qTokens = tokens(query);

    return pool.stream()
            .map(c -> Map.entry(c, score(qTokens, c.title(), c.text())))
            .sorted((a,b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(topK)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
  }


  private static final Set<String> STOP = Set.of(
          "the","a","an","and","or","of","for","to","in","on","by","is","are","was","were","with","at","as","from","that","this","it"
  );
  private static final Pattern WORD = Pattern.compile("[A-Za-z0-9+.%]+");

  private static int score(Set<String> q, String title, String body) {
    var t = tokens(title);
    var b = tokens(body);
    return overlap(q,t)*3 + overlap(q,b);
  }

  private static int overlap(Set<String> a, Set<String> b) {
    int s = 0; for (String t : a) if (b.contains(t)) s++; return s;
  }

  private static Set<String> tokens(String s) {
    var m = WORD.matcher(s.toLowerCase());
    Set<String> out = new HashSet<>();
    while (m.find()) {
      String t = m.group();
      if (!STOP.contains(t) && t.length() > 1) out.add(t);
    }
    return out;
  }
}
