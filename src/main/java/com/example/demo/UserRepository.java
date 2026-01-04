package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * JPA repository for User entity database operations.
 * Spring Data JPA provides built-in CRUD methods without writing SQL.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Checks if a user with the given email exists.
     *
     * @param email email to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds a user by their email address.
     *
     * @param email email to search for
     * @return Optional containing user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);
}
