package com.apple.salesassistant.auth.handler;

import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@Slf4j
public class LoggingAuthenticationEntryPoint implements AuthenticationEntryPoint {
  @Override
  public void commence(HttpServletRequest req, HttpServletResponse res,
                       AuthenticationException ex) throws IOException {
    log.info("Unauthorized access attempt detected path=%s reason=%s%n".formatted(req.getRequestURI(), ex.getMessage()));
    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
  }
}
