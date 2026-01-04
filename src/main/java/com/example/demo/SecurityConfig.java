package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for JWT-based authentication.
 * Configures public/protected endpoints and disables unnecessary features for REST API.
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Provides BCrypt password encoder bean for hashing passwords.
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures HTTP security with JWT authentication.
     * Disables CSRF, form login, and basic auth for stateless REST API.
     * Public: /api/v1/auth/**, static resources. Protected: all other endpoints.
     *
     * @param http HttpSecurity configuration object
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API → disable CSRF
                .csrf(csrf -> csrf.disable())

                // Disable default login page
                .formLogin(form -> form.disable())

                // Disable basic auth
                .httpBasic(basic -> basic.disable())

                // Add JWT filter before Spring Security's authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()  // Allow login/register
                        .requestMatchers("/", "/index.html", "/*.css", "/*.js", "/static/**").permitAll()  // Allow static resources
                        .anyRequest().authenticated()  // Protect all API endpoints
                );

        return http.build();
    }
}
