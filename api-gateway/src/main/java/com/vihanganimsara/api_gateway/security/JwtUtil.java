package com.vihanganimsara.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    @Value("${jwt.secret:your-secret-key-minimum-256-bits-long-for-HS256-algorithm}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Validate and extract claims from JWT token
     * @param token JWT token string
     * @return Claims if valid, null otherwise
     */
    public Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if token is valid
     * @param token JWT token string
     * @return true if valid and not expired
     */
    public boolean isTokenValid(String token) {
        Claims claims = extractClaims(token);
        if (claims == null) {
            return false;
        }
        
        // Check if token is not expired
        return !claims.getExpiration().before(new java.util.Date());
    }

    /**
     * Extract username from token
     * @param token JWT token string
     * @return username or null if invalid
     */
    public String extractUsername(String token) {
        Claims claims = extractClaims(token);
        return claims != null ? claims.getSubject() : null;
    }
}
