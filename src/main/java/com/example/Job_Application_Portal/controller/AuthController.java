package com.example.Job_Application_Portal.controller;

import com.example.Job_Application_Portal.exception.AuthenticationFailedException;
import com.example.Job_Application_Portal.exception.DuplicateResourceException;
import com.example.Job_Application_Portal.model.User;
import com.example.Job_Application_Portal.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Show the registration page
    @GetMapping("/register")
    public String showRegisterPage(Model model) {

        model.addAttribute("user", new User());

        return "register";
    }

    // Process the registration form
    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            Model model
    ) {

        // Check validation errors from the User entity
        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(user);

            return "redirect:/login?registered";

        } catch (DuplicateResourceException exception) {

            model.addAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "register";
        }
    }

    // Show the login page
    @GetMapping("/login")
    public String showLoginPage() {

        return "login";
    }

    // Process the login form
    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session, // represent user session, server can remember when they move to other page
            Model model
    ) {

        try {
            User loginUser = new User();
            loginUser.setEmail(email);
            loginUser.setPassword(password);

            User user = userService.loginUser(loginUser);

            // Log in user stored in the session
            // Even the user moves to another page, the server still knows that the user is logged
            session.setAttribute("loggedInUser", user);

            // Redirect based on the user's role
            if ("ADMIN".equalsIgnoreCase(
                    String.valueOf(user.getRole())
            )) {
                return "redirect:/admin/dashboard";
            }

            return "redirect:/applicant/dashboard";

        } catch (AuthenticationFailedException exception) {

            model.addAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "login";
        }
    }

    // Log the user out
    @PostMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/login?logout";
    }
}
