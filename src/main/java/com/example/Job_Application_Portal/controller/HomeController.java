package com.example.Job_Application_Portal.controller;

import com.example.Job_Application_Portal.model.User;
import com.example.Job_Application_Portal.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final UserService userService;

    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        User loggedInUser = refreshLoggedInUser(session);
        model.addAttribute("loggedInUser", loggedInUser);
        return "index";
    }

    private User refreshLoggedInUser(HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return null;
        }

        try {
            User currentUser = userService.getUserById(sessionUser.getUserId());
            session.setAttribute("loggedInUser", currentUser);
            return currentUser;
        } catch (RuntimeException exception) {
            session.invalidate();
            return null;
        }
    }
}
