package com.apple.salesassistant.auth.service;

import com.apple.salesassistant.auth.model.User;
import com.apple.salesassistant.auth.model.UserStore;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserStore userStore;
  private final TokenService tokenService;
  private final BCryptPasswordEncoder enc = new BCryptPasswordEncoder();

  public AuthService(UserStore userStore, TokenService tokenService) {
    this.userStore = userStore;
    this.tokenService = tokenService;
  }

  public record AuthResult(User user, String jwt, long expiresInSeconds) {}

  public AuthResult authenticate(String email, String password) {
    var user = userStore.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("Invalid credentials"));

    if (!enc.matches(password, user.passwordHash())) {
      throw new RuntimeException("Invalid credentials");
    }

    var jwt = tokenService.issue(user);
    long ttl = tokenService.expiresInSeconds();   // helper for UI/clients
    return new AuthResult(user, jwt, ttl);
  }
}
