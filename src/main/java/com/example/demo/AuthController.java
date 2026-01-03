package com.example.demo;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication operations.
 * Handles user registration and login at /api/v1/auth endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {


    ///  all 3 dependencies are beans
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository,
                         JwtService jwtService,
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {

        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().build(); // for now only sends a bad request 400 (no body) need to fix that later
        }

        // Create new user
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        // Save to database
        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtService.generateToken(savedUser);

        // Build response
        AuthResponse response = new AuthResponse(
            token,
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getName()
        );

        return ResponseEntity.ok(response); // this is a status with body the response
    }

}
