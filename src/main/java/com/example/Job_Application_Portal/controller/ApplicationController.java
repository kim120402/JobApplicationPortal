package com.example.Job_Application_Portal.controller;

import com.example.Job_Application_Portal.service.ApplicationService;
import com.example.Job_Application_Portal.service.JobService;
import org.springframework.stereotype.Controller;

@Controller
public class ApplicationController {
    private final ApplicationService applicationService;
    private final JobService jobService;

    public ApplicationController(ApplicationService applicationService, JobService jobService) {
        this.applicationService = applicationService;
        this.jobService = jobService;
    }
}
