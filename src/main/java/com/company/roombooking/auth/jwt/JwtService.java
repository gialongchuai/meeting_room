package com.company.roombooking.auth.jwt;

import com.company.roombooking.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private final String secret;
    private final long accessTtlMinutes;
    private final long refreshTtlDays;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-ttl-minutes:30}") long accessTtlMinutes,
            @Value("${jwt.refresh-ttl-days:7}") long refreshTtlDays) {
        this.secret = secret;
        this.accessTtlMinutes = accessTtlMinutes;
        this.refreshTtlDays = refreshTtlDays;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public long getAccessTtlMillis() {
        return accessTtlMinutes * 60 * 1000L;
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("role", user.getRole().name());
        claims.put("mustChangePassword", user.isMustChangePassword());
        claims.put("tokenType", "ACCESS");

        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(now))
                .expiration(new Date(now + getAccessTtlMillis()))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("tokenType", "REFRESH");

        long now = System.currentTimeMillis();
        long refreshTtlMillis = refreshTtlDays * 24 * 60 * 60 * 1000L;
        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(now))
                .expiration(new Date(now + refreshTtlMillis()))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long extractUserId(String token) {
        Claims claims = parseClaims(token);
        Object userId = claims.get("userId");
        if (userId instanceof Number num) {
            return num.longValue();
        }
        return null;
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractTokenType(String token) {
        return (String) parseClaims(token).get("tokenType");
    }

    public boolean extractMustChangePassword(String token) {
        Boolean value = (Boolean) parseClaims(token).get("mustChangePassword");
        return Boolean.TRUE.equals(value);
    }
}
