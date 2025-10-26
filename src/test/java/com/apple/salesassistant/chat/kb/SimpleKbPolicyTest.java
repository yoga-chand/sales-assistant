package com.apple.salesassistant.chat.kb;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

record Auth(String userId, Set<String> roles, Set<String> tags) {}
record KbChunk(String id, String title, String body, Set<String> tags) {}

class SimpleKbPolicy {
  boolean canSee(Auth a, KbChunk c) {
    boolean isGuest = a.roles().contains("ROLE_GUEST");
    boolean detailed = c.tags().contains("detailed");
    if (isGuest && detailed) return false;
    if (!a.tags().isEmpty() && c.tags().stream().noneMatch(a.tags()::contains)) return false;
    return true;
  }
}

public class SimpleKbPolicyTest {
  @Test
  void guestCannotSeeDetailed() {
    var policy = new SimpleKbPolicy();
    var guest = new Auth("guest", Set.of("ROLE_GUEST"), Set.of());
    var detailed = new KbChunk("1","iPhone deep dive","...", Set.of("iphone","detailed"));
    assertThat(policy.canSee(guest, detailed)).isFalse();
  }
  @Test
  void analystCanSeeDetailed() {
    var policy = new SimpleKbPolicy();
    var analyst = new Auth("u1", Set.of("ROLE_ANALYST"), Set.of("iphone"));
    var detailed = new KbChunk("1","iPhone deep dive","...", Set.of("iphone","detailed"));
    assertThat(policy.canSee(analyst, detailed)).isTrue();
  }
  @Test
  void tagMismatchDenied() {
    var policy = new SimpleKbPolicy();
    var analyst = new Auth("u1", Set.of("ROLE_ANALYST"), Set.of("apac"));
    var emeaDoc = new KbChunk("2","Mac EMEA","...", Set.of("emea","mac"));
    assertThat(policy.canSee(analyst, emeaDoc)).isFalse();
  }
}
