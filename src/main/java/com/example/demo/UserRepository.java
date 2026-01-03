package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for User entity database operations.
 * Spring Data JPA provides built-in CRUD methods without writing SQL.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}
