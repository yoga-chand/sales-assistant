package com.apple.salesassistant.auth.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Principal;

@Slf4j
@Component
public class AuditLoggingFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        var principal = req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : "anonymous";
        String method = req.getMethod();
        String uri = req.getRequestURI();
        long start = System.currentTimeMillis();

        chain.doFilter(req, res);

        long duration = System.currentTimeMillis() - start;
        log.info("[AUDIT] user={} method={} uri={} status={} duration={}ms",
                principal == null ? "GUEST" : principal,
                method, uri, res.getStatus(), duration);
    }
}
