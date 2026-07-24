package com.example.Job_Application_Portal.controller;

import com.example.Job_Application_Portal.service.ApplicationService;
import com.example.Job_Application_Portal.service.JobService;
import com.example.Job_Application_Portal.service.UserService;
import org.springframework.stereotype.Controller;

@Controller
public class AdminController {
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final UserService userService;

    public AdminController(
            JobService jobService,
            ApplicationService applicationService,
            UserService userService
    ) {
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.userService = userService;
    }
}
