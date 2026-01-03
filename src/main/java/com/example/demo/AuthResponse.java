package com.example.demo;

public record AuthResponse(
        String token,          // Required: The JWT access token. The client stores it and sends it in future requests
        String type,           // Required: Always "Bearer" (client uses in Authorization header)
        Long expiresIn,        // Optional: Token expiry in seconds (e.g., 3600 for 1 hour)
        Long userId,           // Required: ID of the created/authenticated user
        String email,          // Required: User's email
        String name            // Optional: User's name (include if provided during register)
) {

    // Convenience constructor for cases where you want defaults
    public AuthResponse(String token, Long userId, String email, String name) {
        this(token, "Bearer", 3600L, userId, email, name);  // Default 1-hour expiry
    }

    // Or another without name if null
    public AuthResponse(String token, Long userId, String email) {
        this(token, "Bearer", 3600L, userId, email, null);
    }
}