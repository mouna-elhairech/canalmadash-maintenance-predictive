package com.canal.canalmadash.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${JWT_SECRET}") String secret,
                      @Value("${app.jwt.expiration-minutes:480}") long expirationMinutes) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET doit contenir au moins 32 caracteres");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMs = expirationMinutes * 60_000L;
    }

    public String generer(String email, String role, List<String> fonctionnalites) {
        Date maintenant = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("fonctionnalites", fonctionnalites)
                .issuedAt(maintenant)
                .expiration(new Date(maintenant.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public Optional<Claims> verifier(String token) {
        try {
            return Optional.of(Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}