package com.example.Job_Application_Portal.controller;

import com.example.Job_Application_Portal.model.Job;
import com.example.Job_Application_Portal.service.JobService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/job")
    public String showCreateJobForm(Model model) {

        model.addAttribute("job", new Job());

        return "create-job";
    }

    @PostMapping("/job")
    public String createJob(
            @Valid @ModelAttribute("job") Job job,
            BindingResult result,
            Model model
    ) {
        // If validation errors are found, return to the form
        if (result.hasErrors()) {
            return "create-job";
        }

        try {
            jobService.createJob(job);

            return "redirect:/jobs";

        } catch (RuntimeException exception) {
            model.addAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "create-job";
        }
    }
}
