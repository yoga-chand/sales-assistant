package com.apple.salesassistant.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProps(String issuer, String secret, int expiresMinutes) {}
