package com.example.Job_Application_Portal.service;

import com.example.Job_Application_Portal.exception.AuthenticationFailedException;
import com.example.Job_Application_Portal.exception.DuplicateResourceException;
import com.example.Job_Application_Portal.exception.ResourceNotFoundException;
import com.example.Job_Application_Portal.exception.ValidationException;
import com.example.Job_Application_Portal.model.User;
import com.example.Job_Application_Portal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private static final String DEFAULT_ROLE = "APPLICANT";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {
        validateRegistration(user);

        String email = normalize(user.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists");
        }

        user.setFullName(user.getFullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(DEFAULT_ROLE);

        return userRepository.save(user);
    }

    public User loginUser(User loginUser) {
        validateLogin(loginUser);

        User existingUser = userRepository.findByEmail(normalize(loginUser.getEmail()))
                .orElseThrow(() -> new AuthenticationFailedException("Invalid email or password"));

        if (!passwordEncoder.matches(loginUser.getPassword(), existingUser.getPassword())) {
            throw new AuthenticationFailedException("Invalid email or password");
        }

        return existingUser;
    }

    public User getUserById(Long userId) {
        if (userId == null) {
            throw new ValidationException("User id is required");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User getUserByEmail(String email) {
        if (isBlank(email)) {
            throw new ValidationException("Email is required");
        }

        return userRepository.findByEmail(normalize(email))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public long countUsersByRole(String role) {
        if (isBlank(role)) {
            throw new ValidationException("Role is required");
        }

        return userRepository.countByRoleIgnoreCase(role.trim());
    }

    private void validateRegistration(User user) {
        if (user == null) {
            throw new ValidationException("User is required");
        }
        if (isBlank(user.getFullName())) {
            throw new ValidationException("Full name is required");
        }
        if (isBlank(user.getEmail())) {
            throw new ValidationException("Email is required");
        }
        if (isBlank(user.getPassword())) {
            throw new ValidationException("Password is required");
        }
        if (user.getPassword().length() < 6) {
            throw new ValidationException("Password must contain at least 6 characters");
        }
    }

    private void validateLogin(User user) {
        if (user == null) {
            throw new ValidationException("User is required");
        }
        if (isBlank(user.getEmail())) {
            throw new ValidationException("Email is required");
        }
        if (isBlank(user.getPassword())) {
            throw new ValidationException("Password is required");
        }
    }

    private String normalize(String value) {
        return value.trim().toLowerCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
