package com.apple.salesassistant.chat.kb;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

record Chunk(String id, String title, String body) {}

class SimpleRetriever {
  // toy “score” = how many times a token appears
  List<Chunk> topK(String q, int k, List<Chunk> all) {
    return all.stream()
      .sorted(Comparator.comparingInt((Chunk c) -> score(q, c)).reversed())
      .limit(k).toList();
  }
  private int score(String q, Chunk c) {
    int s=0; for (var t : q.toLowerCase().split("\\s+")) {
      if (c.title().toLowerCase().contains(t)) s++;
      if (c.body().toLowerCase().contains(t)) s++;
    } return s;
  }
}

public class SimpleRetrieverTest {
  @Test
  void ranksByScoreAndLimitsToK() {
    var retriever = new SimpleRetriever();
    var docs = List.of(
      new Chunk("1","iPhone Summary","iphone grows"),
      new Chunk("2","Mac Trends","mac stable"),
      new Chunk("3","Services","services up")
    );
    var top = retriever.topK("iphone", 2, docs);
    assertThat(top).extracting(Chunk::id).containsExactly("1","2"); // 2nd arbitrary by tie
  }
}
