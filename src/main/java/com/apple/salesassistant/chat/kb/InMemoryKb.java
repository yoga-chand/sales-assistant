package com.apple.salesassistant.chat.kb;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class InMemoryKb {

  private final ResourceLoader loader;
  private final String path;
  private final int chunkSize;
  private List<KbChunk> chunks;

  public InMemoryKb(ResourceLoader loader, org.springframework.core.env.Environment env) {
    this.loader = loader;
    this.path = env.getProperty("kb.file-path", "classpath:kb/kb.txt");
    this.chunkSize = Integer.parseInt(env.getProperty("kb.chunk-size", "700"));
  }

  @PostConstruct
  public void load() throws Exception {
    String all = readAll();                          // <— here’s readAll()
    List<Section> sections = splitByHeadings(all);
    List<KbChunk> out = new ArrayList<>();
    int seq = 0;

    for (Section s : sections) {
      List<String> pieces = splitBySize(s.body(), chunkSize);
      for (String piece : pieces) {
        var minRole = inferMinRole(s.title());
        var scope   = inferScope(s.title());
        var tags    = inferTags(s.title(), piece);
        out.add(new KbChunk(
                UUID.nameUUIDFromBytes(("mem-" + (seq++)).getBytes(StandardCharsets.UTF_8)),
                "kb.txt",
                s.title(),
                piece.trim(),
                null,                // tenantId
                scope,
                minRole,
                tags
        ));
      }
    }
    this.chunks = List.copyOf(out);
  }

  public List<KbChunk> all() { return chunks; }

  // ----- helpers -----

  private String readAll() throws Exception {        // <— loads file from classpath or absolute path
    Resource r = loader.getResource(path);
    if (!r.exists()) throw new IllegalStateException("KB not found: " + path);
    try (var in = r.getInputStream()) {
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private record Section(String title, String body) {}

  private static List<Section> splitByHeadings(String text) {
    List<Section> sections = new ArrayList<>();
    String[] lines = text.split("\\R");
    String currentTitle = "General";
    StringBuilder buf = new StringBuilder();
    Pattern heading = Pattern.compile("^\\s*#+\\s+(.+)$");

    for (String line : lines) {
      var m = heading.matcher(line);
      if (m.find()) {
        if (buf.length() > 0) {
          sections.add(new Section(currentTitle, buf.toString().trim()));
          buf.setLength(0);
        }
        currentTitle = m.group(1).trim();
      } else {
        buf.append(line).append('\n');
      }
    }
    if (buf.length() > 0) sections.add(new Section(currentTitle, buf.toString().trim()));
    return sections;
  }

  private static List<String> splitBySize(String body, int size) {
    if (body.length() <= size) return List.of(body);
    List<String> out = new ArrayList<>();
    int i = 0;
    while (i < body.length()) {
      int end = Math.min(i + size, body.length());
      int cut = body.lastIndexOf("\n\n", end);
      if (cut <= i) cut = end;
      out.add(body.substring(i, cut));
      i = cut;
    }
    return out;
  }

  private static KbChunk.MinRole inferMinRole(String title) {
    String t = title.toLowerCase();
    if (t.contains("confidential") || t.contains("internal")) return KbChunk.MinRole.ADMIN;
    if (t.contains("detail") || t.contains("sku") || t.contains("unit")) return KbChunk.MinRole.ANALYST;
    return KbChunk.MinRole.GUEST;
  }

  private static KbChunk.AccessScope inferScope(String title) {
    String t = title.toLowerCase();
    if (t.contains("executive") || t.contains("summary")) return KbChunk.AccessScope.AGGREGATE;
    if (t.contains("confidential")) return KbChunk.AccessScope.CONFIDENTIAL;
    return KbChunk.AccessScope.DETAIL;
  }

  private static Set<String> inferTags(String title, String body) {
    var tags = new HashSet<String>();
    String s = (title + " " + body).toLowerCase();
    if (s.contains("iphone")) tags.add("iphone");
    if (s.contains("services")) tags.add("services");
    if (s.contains("apac")) tags.add("apac");
    if (s.contains("emea")) tags.add("emea");
    if (s.contains("amer")) tags.add("amer");
    return Set.copyOf(tags);
  }
}
