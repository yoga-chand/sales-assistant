package com.apple.salesassistant.configuration;

import com.apple.salesassistant.auth.api.filter.JwtAuthFilter;
import com.apple.salesassistant.auth.handler.LoggingAccessDeniedHandler;
import com.apple.salesassistant.auth.handler.LoggingAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// com.apple.security.SecurityConfig.java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthFilter jwt;
  private final LoggingAccessDeniedHandler loggingAccessDeniedHandler;

  private final LoggingAuthenticationEntryPoint loggingAuthenticationEntryPoint;

  public SecurityConfig(JwtAuthFilter jwt, LoggingAccessDeniedHandler loggingAccessDeniedHandler, LoggingAuthenticationEntryPoint loggingAuthenticationEntryPoint) { this.jwt = jwt;
    this.loggingAccessDeniedHandler = loggingAccessDeniedHandler;
    this.loggingAuthenticationEntryPoint = loggingAuthenticationEntryPoint;
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(cs -> cs.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/v1/auth/**").permitAll()
                    .requestMatchers("/error", "/error/**").permitAll()
                    // Allow anonymous guests to ask a question
                    .requestMatchers(HttpMethod.POST, "/v1/messages:complete").permitAll()
                    // Everything else requires authentication
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(e -> e
                    .authenticationEntryPoint(loggingAuthenticationEntryPoint)
                    .accessDeniedHandler(loggingAccessDeniedHandler)   )
            // make anonymous explicit (defaults to ROLE_ANONYMOUS)
            .anonymous(anon -> anon.key("guest-anon-key"));
    return http.build();
  }
}

