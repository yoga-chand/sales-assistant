package com.apple.salesassistant.auth.api.filter;

import com.apple.salesassistant.auth.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final TokenService tokens;
  public JwtAuthFilter(TokenService tokens) { this.tokens = tokens; }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String ah = req.getHeader("Authorization");
    if (ah != null && ah.startsWith("Bearer ")) {
      String jwt = ah.substring(7);
      try {
        var jws = tokens.parse(jwt);
        Claims c = jws.getBody();
        String userId = c.getSubject();
        String email = c.get("email", String.class);
        @SuppressWarnings("unchecked")
        var roles = (java.util.List<String>) c.get("roles");
        @SuppressWarnings("unchecked") var scopes = (java.util.List<String>) c.get("scopes");
        Set<GrantedAuthority> auths = new java.util.HashSet<>();
        if (roles  != null) roles.forEach(r  -> auths.add(new SimpleGrantedAuthority(r)));
        if (scopes != null) scopes.forEach(s -> auths.add(new SimpleGrantedAuthority(s)));

        var principal = new AuthUser(userId, email, roles, scopes);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, auths);
        SecurityContextHolder.getContext().setAuthentication(auth);
      } catch (Exception e) {
        log.error("JWT auth failed: {}", e.getMessage());
      }
    }
    chain.doFilter(req, res);
  }

  public record AuthUser(String id, String email, java.util.List<String> roles, java.util.List<String> scopes) {}

}
