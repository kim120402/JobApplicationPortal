package com.example.Job_Application_Portal.repository;

import com.example.Job_Application_Portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository <User, Long>{

    // Returns true if the email already exists in the database
    boolean existsByEmail(String email);

    // Finds and returns a User by email; returns empty if no user is found
    Optional<User> findByEmail(String email);

    long countByRoleIgnoreCase(String role);

}
