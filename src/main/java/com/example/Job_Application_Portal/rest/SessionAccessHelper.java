package com.example.Job_Application_Portal.rest;

import com.example.Job_Application_Portal.exception.ForbiddenAccessException;
import com.example.Job_Application_Portal.exception.UnauthorizedAccessException;
import com.example.Job_Application_Portal.model.User;
import com.example.Job_Application_Portal.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class SessionAccessHelper {
    private final UserService userService;

    public SessionAccessHelper(UserService userService) {
        this.userService = userService;
    }

    // Check if someone is log in
    public User requireLoggedInUser(HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");

        // If there is no logged-in user in the session, block the request
        if (sessionUser == null) {
            throw new UnauthorizedAccessException("Login is required");
        }

        try {
            // this retrieve the user from database to make sure account still exist
            return userService.getUserById(sessionUser.getUserId());
        } catch (RuntimeException exception) {
            session.invalidate(); // destroy old session
            throw new UnauthorizedAccessException("Login is required");
        }
    }

    // This method checks whether the logged-in user is an Admin.
    public User requireAdmin(HttpSession session) {

        // before checking the role, it first makes sure the user is logged in.
        User user = requireLoggedInUser(session);

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new ForbiddenAccessException("Admin access is required");
        }

        return user;
    }

    public User requireApplicant(HttpSession session) {

        // before checking the role, it first makes sure the user is logged in.
        User user = requireLoggedInUser(session);

        if (!"APPLICANT".equalsIgnoreCase(user.getRole())) {
            throw new ForbiddenAccessException("Applicant access is required");
        }

        return user;
    }
}
