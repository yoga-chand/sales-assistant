// com.apple.kb.KbPolicy.java
package com.apple.salesassistant.chat.kb;


import com.apple.salesassistant.auth.dto.UserContext;

public final class KbPolicy {
  private KbPolicy() {}

  public static boolean canSee(UserContext user, KbChunk c) {
    // role threshold
    int need = switch (c.minRole()) {
      case ADMIN -> 3;
      case ANALYST -> 2;
      case GUEST -> 1;
    };
    if (user.roleLevel() < need) return false;

    // tenant scoping
    if (c.tenantId() != null && !c.tenantId().equals(user.tenantId())) return false;

    // optional tag scoping
    if (user.allowedTags() != null && !user.allowedTags().isEmpty()) {
      boolean any = c.tags().stream().anyMatch(user.allowedTags()::contains);
      if (!any) return false;
    }
    return true;
  }
}
