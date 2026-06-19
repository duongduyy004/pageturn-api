package com.pageturn.backend.security;

import com.pageturn.backend.config.AppConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final AppConfig appConfig;
    private final SecretKey secretKey;

    public JwtService(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(appConfig.jwt().secret()));
    }

    public String generateAccessToken(Long userId, String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim(SecurityConstants.CLAIM_USER_ID, userId)
                .claim(SecurityConstants.CLAIM_ROLE, role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(appConfig.jwt().accessTokenTtlSeconds())))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public Long extractUserId(String token) {
        return parseClaims(token).get(SecurityConstants.CLAIM_USER_ID, Long.class);
    }

    public String extractRole(String token) {
        return parseClaims(token).get(SecurityConstants.CLAIM_ROLE, String.class);
    }

    public boolean isTokenExpired(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration == null || expiration.before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }
}
