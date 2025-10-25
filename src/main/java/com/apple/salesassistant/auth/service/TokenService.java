package com.apple.salesassistant.auth.service;

import com.apple.salesassistant.auth.util.RolePolicy;
import com.apple.salesassistant.auth.model.User;
import com.apple.salesassistant.configuration.JwtProps;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class TokenService {

    private final JwtProps props;
    private final byte[] key;

    public TokenService(JwtProps props) {
        this.props = props;
        this.key = props.secret().getBytes(StandardCharsets.UTF_8);
    }

    public String issue(User user) {
        var scopes = RolePolicy.scopesForRoles(user.roles());
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.expiresMinutes() * 60L);
        return Jwts.builder()
                .setIssuer(props.issuer())
                .setSubject(user.id())
                .setAudience("api")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("email", user.email())
                .claim("roles", user.roles())
                .claim("scopes", scopes)
                .signWith(Keys.hmacShaKeyFor(key), SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(key))
                .requireIssuer(props.issuer())
                .build()
                .parseClaimsJws(jwt);
    }

    public long expiresInSeconds() { return props.expiresMinutes() * 60L; }
}
