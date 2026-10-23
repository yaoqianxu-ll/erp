package com.erp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${erp.jwt.secret}")
    private String secret;

    @Value("${erp.jwt.expire}")
    private long expire;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generate(LoginUser user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("uid", user.getId())
                .claim("nickname", user.getNickname())
                .claim("roles", user.getRoles())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expire))
                .signWith(key)
                .compact();
    }

    @SuppressWarnings("unchecked")
    public LoginUser parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        List<String> roles = claims.get("roles", List.class);
        return new LoginUser(
                claims.get("uid", Number.class).longValue(),
                claims.getSubject(),
                claims.get("nickname", String.class),
                roles == null ? List.of() : (List<String>) roles
        );
    }
}
