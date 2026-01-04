package com.example.demo;

/**
 * Response payload for the profile endpoint.
 * Returns authenticated user's basic information.
 */
public record ProfileResponse(
        Long id,
        String name,
        String email
) {}
