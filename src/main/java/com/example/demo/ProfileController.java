package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user profile operations.
 * Provides endpoints for retrieving authenticated user information.
 */
@RestController
@RequestMapping("/api/v1")
public class ProfileController {

    /**
     * Retrieves profile data for the authenticated user.
     * Requires valid JWT authentication.
     *
     * @param user authenticated user injected by Spring Security
     * @return profile data including user ID, name, and email
     */
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal User user) {

        ProfileResponse response = new ProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );

        return ResponseEntity.ok(response);
    }
}
