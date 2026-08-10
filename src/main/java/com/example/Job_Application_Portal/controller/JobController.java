package com.example.Job_Application_Portal.controller;

import com.example.Job_Application_Portal.model.Job;
import com.example.Job_Application_Portal.service.JobService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/jobs")
    public String showJobs(HttpSession session, Model model) {
        model.addAttribute("jobs", jobService.getActiveJobs());
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
        return "jobs";
    }

    @GetMapping("/jobs/{jobId}")
    public String showJobDetails(@PathVariable Long jobId, HttpSession session, Model model) {
        model.addAttribute("job", jobService.getJobById(jobId));
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
        return "job-details";
    }

    @GetMapping("/jobs/search")
    public String searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String employmentType,
            HttpSession session,
            Model model
    ) {
        List<Job> jobs = jobService.getActiveJobs().stream()
                .filter(job -> containsIgnoreCase(job.getTitle(), title))
                .filter(job -> containsIgnoreCase(job.getLocation(), location))
                .filter(job -> equalsIgnoreCase(job.getCategory(), category))
                .filter(job -> equalsIgnoreCase(job.getEmploymentType(), employmentType))
                .toList();

        model.addAttribute("jobs", jobs);
        model.addAttribute("title", title);
        model.addAttribute("location", location);
        model.addAttribute("category", category);
        model.addAttribute("employmentType", employmentType);
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
        return "jobs";
    }

    private boolean containsIgnoreCase(String value, String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return true;
        }

        return value != null && value.toLowerCase().contains(searchTerm.trim().toLowerCase());
    }

    private boolean equalsIgnoreCase(String value, String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return true;
        }

        return value != null && value.equalsIgnoreCase(searchTerm.trim());
    }
}
