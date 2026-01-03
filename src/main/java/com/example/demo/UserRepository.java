package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

// will be used to access the User database (spring provides you with methods to check everything
// you need so no sql code need to be writen)
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}
