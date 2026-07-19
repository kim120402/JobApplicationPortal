package com.example.Job_Application_Portal.service;

import com.example.Job_Application_Portal.exception.AuthenticationFailedException;
import com.example.Job_Application_Portal.exception.DuplicateResourceException;
import com.example.Job_Application_Portal.exception.ValidationException;
import com.example.Job_Application_Portal.model.User;
import com.example.Job_Application_Portal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void registerUserHashesPasswordAndDefaultsRole() {
        User user = new User();
        user.setFullName(" Jane Doe ");
        user.setEmail(" JANE@example.com ");
        user.setPassword("secret123");
        user.setRole("ADMIN");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = userService.registerUser(user);

        assertThat(registeredUser.getFullName()).isEqualTo("Jane Doe");
        assertThat(registeredUser.getEmail()).isEqualTo("jane@example.com");
        assertThat(registeredUser.getRole()).isEqualTo("APPLICANT");
        assertThat(registeredUser.getPassword()).isNotEqualTo("secret123");
        assertThat(passwordEncoder.matches("secret123", registeredUser.getPassword())).isTrue();
    }

    @Test
    void registerUserRejectsDuplicateEmail() {
        User user = validUser();
        when(userRepository.existsByEmail("student@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUserRejectsMissingRequiredData() {
        User user = validUser();
        user.setFullName(" ");

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Full name is required");
    }

    @Test
    void loginUserReturnsExistingUserWhenPasswordMatches() {
        User storedUser = validUser();
        storedUser.setPassword(passwordEncoder.encode("secret123"));
        User loginUser = new User();
        loginUser.setEmail(" STUDENT@example.com ");
        loginUser.setPassword("secret123");

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(storedUser));

        User result = userService.loginUser(loginUser);

        assertThat(result).isSameAs(storedUser);
    }

    @Test
    void loginUserRejectsBadPassword() {
        User storedUser = validUser();
        storedUser.setPassword(passwordEncoder.encode("secret123"));
        User loginUser = new User();
        loginUser.setEmail("student@example.com");
        loginUser.setPassword("wrong-password");

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(storedUser));

        assertThatThrownBy(() -> userService.loginUser(loginUser))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Invalid email or password");
    }

    private User validUser() {
        User user = new User();
        user.setFullName("Student User");
        user.setEmail("student@example.com");
        user.setPassword("secret123");
        user.setRole("APPLICANT");
        return user;
    }
}
