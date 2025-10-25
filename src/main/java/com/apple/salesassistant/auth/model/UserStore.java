package com.apple.salesassistant.auth.model;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class UserStore {
  private final Map<String, User> byEmail = new HashMap<>();
  private final BCryptPasswordEncoder enc = new BCryptPasswordEncoder();

  public UserStore() {
    byEmail.put("guest@demo",   new User("u1","guest@demo",   enc.encode("guest"),   Set.of("ROLE_GUEST")));
    byEmail.put("analyst@demo", new User("u2","analyst@demo", enc.encode("analyst"), Set.of("ROLE_ANALYST")));
    byEmail.put("admin@demo",   new User("u3","admin@demo",   enc.encode("admin"),   Set.of("ROLE_ADMIN")));
  }
  public Optional<User> findByEmail(String email){ return Optional.ofNullable(byEmail.get(email)); }
}
