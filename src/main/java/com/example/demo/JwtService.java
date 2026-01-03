package com.example.demo;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Service for JWT token generation and validation.
 * Creates signed tokens with user claims and 1-hour expiration using HS256 algorithm.
 */
@Service
public class JwtService {

    /// Move JWT secret to application.yml
    private static final String SECRET =
            "CHANGE_THIS_TO_A_LONG_RANDOM_SECRET_256_BITS_MIN";

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000)) // 1 hour
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), Jwts.SIG.HS256)
                .compact();
    }
}
