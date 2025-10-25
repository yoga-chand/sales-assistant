package com.apple.salesassistant.auth.handler;

import jakarta.servlet.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class LoggingAccessDeniedHandler implements AccessDeniedHandler {
  @Override
  public void handle(HttpServletRequest req, HttpServletResponse res,
                     AccessDeniedException ex) throws IOException {
    var principal = req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : "anonymous";
    var roles = req.isUserInRole("ROLE_ADMIN") ? "ROLE_ADMIN"
             : req.isUserInRole("ROLE_ANALYST") ? "ROLE_ANALYST"
             : req.isUserInRole("ROLE_GUEST") ? "ROLE_GUEST"
             : "unknown";
    String authz = req.getHeader("Authorization");
    // Log the 403 event
    System.out.printf("403 FORBIDDEN path=%s principal=%s roles=%s reason=%s authHeader=%s%n",
        req.getRequestURI(), principal, roles, ex.getMessage(), authz != null ? "present" : "absent");
    res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
  }
}
