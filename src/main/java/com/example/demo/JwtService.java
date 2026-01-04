package com.example.demo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Service for JWT token generation and validation.
 * Creates signed tokens with user claims and configurable expiration using HS256 algorithm.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationTime;

    /**
     * Generates a JWT token for the given user.
     * Token includes user ID as subject, email as claim, and expiration time.
     *
     * @param user user for whom to generate the token
     * @return signed JWT token string
     */
    public String generateToken(User user) {
            return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Validates JWT token signature and extracts claims.
     *
     * @param token JWT token to validate
     * @return claims from the validated token
     * @throws io.jsonwebtoken.JwtException if token is invalid or expired
     */
    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts user ID from a valid JWT token.
     *
     * @param token JWT token to parse
     * @return user ID stored in token subject
     * @throws io.jsonwebtoken.JwtException if token is invalid
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Checks if a token is valid without throwing exceptions.
     *
     * @param token JWT token to check
     * @return true if token is valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
