package com.apple.salesassistant.auth.dto;

import java.util.Set;

public record UserContext(
    String userId,
    String tenantId,
    Set<String> roles,        // ["ROLE_ANALYST"]
    Set<String> scopes,       // ["kb:read:detail"]
    Set<String> allowedTags   // optional: limit per dept, region, etc.
) {
  public int roleLevel() {
    if (roles.contains("ROLE_ADMIN"))   return 3;
    if (roles.contains("ROLE_ANALYST")) return 2;
    return 1; // guest
  }
}
