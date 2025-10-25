// com.apple.security.RolePolicy.java
package com.apple.salesassistant.auth.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RolePolicy {
  private RolePolicy() {}
  public static final String ROLE_GUEST   = "ROLE_GUEST";
  public static final String ROLE_ANALYST = "ROLE_ANALYST";
  public static final String ROLE_ADMIN   = "ROLE_ADMIN";

  // least-privilege scopes per role
  public static final Map<String, Set<String>> SCOPES_BY_ROLE = Map.of(
      ROLE_GUEST,   Set.of("chat:invoke", "kb:read:aggregate", "conv:read", "conv:write"),
      ROLE_ANALYST, Set.of("chat:invoke", "kb:read:detail", "conv:read", "conv:write"),
      ROLE_ADMIN,   Set.of("chat:invoke", "kb:read:detail", "conv:read", "conv:read:any", "conv:write", "admin:model:set", "audit:read")
  );

  public static Set<String> scopesForRoles(Set<String> roles) {
    return roles.stream()
        .flatMap(r -> SCOPES_BY_ROLE.getOrDefault(r, Set.of()).stream())
        .collect(java.util.stream.Collectors.toSet());
  }
}
