// com.apple.security.LoggingAuthenticationEntryPoint.java
package com.apple.salesassistant.auth.handler;

import jakarta.servlet.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class LoggingAuthenticationEntryPoint implements AuthenticationEntryPoint {
  @Override
  public void commence(HttpServletRequest req, HttpServletResponse res,
                       AuthenticationException ex) throws IOException {
    System.out.printf("401 UNAUTHORIZED path=%s reason=%s%n", req.getRequestURI(), ex.getMessage());
    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
  }
}
