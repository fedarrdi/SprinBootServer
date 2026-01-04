package com.example.demo;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

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

    private static final String COOKIE_NAME = "jwt_token";

    @Value("${jwt.expiration}")
    private long TokenExpirationTime;

    @Value("${jwt.cookie.secure}")
    private boolean cookieSecure;

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
            return ResponseEntity
                    .badRequest()
                    .build(); // for now only sends a bad request 400 (no body) need to fix that later
        }

        // Create new user
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        // Save to database
        User savedUser = userRepository.save(user);

       return get_response(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LogInRequest request) {

        // Call the method - pass the email
        Optional<User> userOptional = userRepository.findByEmail(request.email());

        // Check if user exists
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401).build();  // User not found
        }

        // Get the user object
        User user = userOptional.get();

        // Get the hashed password from the user
        String hashedPassword = user.getPasswordHash();

        // Verify password
        boolean isPasswordCorrect = passwordEncoder.matches(request.password(), hashedPassword);

        if (!isPasswordCorrect) {
            return ResponseEntity.status(401).build();  // Wrong password
        }

        return get_response(user);
    }


    private ResponseEntity<AuthResponse> get_response(User user) {
        // Generate JWT token
        String token = jwtService.generateToken(user);

        // Create HTTP-only cookie
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)              // Prevents access via JavaScript (mitigates XSS)
                .secure(cookieSecure)        // Configurable: false for dev (HTTP), true for prod (HTTPS)
                .path("/")                   // Available across entire app
                .maxAge(TokenExpirationTime / 1000) // Convert milliseconds to seconds
                .sameSite("Lax")             // Or "Strict" if you don't need third-party usage
                // .domain("yourdomain.com") // Optional: set if needed for subdomains
                .build();

        // Build response
        AuthResponse response = new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getName()
        );

        /**
         * Returns the authentication response with:
         * - 200 OK status
         * - AuthResponse (token, user details, etc.) as JSON body
         * - JWT token also set in a secure, HTTP-only cookie for automatic future inclusion
         */
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

}
