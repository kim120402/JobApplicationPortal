package com.example.Job_Application_Portal.controller;

import com.example.Job_Application_Portal.model.Application;
import com.example.Job_Application_Portal.model.Job;
import com.example.Job_Application_Portal.model.User;
import com.example.Job_Application_Portal.service.ApplicationService;
import com.example.Job_Application_Portal.service.JobService;
import com.example.Job_Application_Portal.service.ResumeStorageService;
import com.example.Job_Application_Portal.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
public class ApplicationController {
    private final ApplicationService applicationService;
    private final JobService jobService;
    private final ResumeStorageService resumeStorageService;
    private final UserService userService;

    public ApplicationController(
            ApplicationService applicationService,
            JobService jobService,
            ResumeStorageService resumeStorageService,
            UserService userService
    ) {
        this.applicationService = applicationService;
        this.jobService = jobService;
        this.resumeStorageService = resumeStorageService;
        this.userService = userService;
    }

    @GetMapping("/applicant/dashboard")
    public String applicantDashboard(HttpSession session, Model model) {
        User user = requireApplicant(session);
        if (user == null) {
            return loginRedirect(session);
        }

        model.addAttribute("user", user);
        model.addAttribute("totalApplications", applicationService.countApplicationsByUser(user.getUserId()));
        model.addAttribute(
                "underReviewCount",
                applicationService.countUserApplicationsByStatus(user.getUserId(), ApplicationService.STATUS_UNDER_REVIEW)
        );
        model.addAttribute(
                "interviewCount",
                applicationService.countUserApplicationsByStatus(user.getUserId(), ApplicationService.STATUS_INTERVIEW)
        );
        model.addAttribute(
                "hiredCount",
                applicationService.countUserApplicationsByStatus(user.getUserId(), ApplicationService.STATUS_HIRED)
        );
        return "applicant-dashboard";
    }

    @GetMapping("/jobs/{jobId}/apply")
    public String showApplyForm(
            @PathVariable Long jobId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        User user = requireApplicant(session);
        if (user == null) {
            return loginRedirect(session);
        }

        Job job = jobService.getJobById(jobId);
        if (!JobService.STATUS_ACTIVE.equalsIgnoreCase(job.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Applications are only available for active jobs.");
            return "redirect:/jobs/" + jobId;
        }

        model.addAttribute("user", user);
        model.addAttribute("job", job);
        return "apply";
    }

    @PostMapping("/jobs/{jobId}/apply")
    public String submitApplication(
            @PathVariable Long jobId,
            @RequestParam(value = "coverLetter", required = false) String coverLetter,
            @RequestParam("resume") MultipartFile resume,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        // Check the current session and make sure the logged-in user is an Applicant
        User user = requireApplicant(session);
        if (user == null) {
            return loginRedirect(session);
        }

        Job job;
        try {
            job = jobService.getJobById(jobId); // check job id if exist
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Job not found.");
            return "redirect:/jobs";
        }

        if (!JobService.STATUS_ACTIVE.equalsIgnoreCase(job.getStatus())) {
            model.addAttribute("job", job); // send back the job information
            model.addAttribute("errorMessage", "Applications are only available for active jobs.");
            return "apply";
        }

        String savedFileName = null;
        try {
            savedFileName = resumeStorageService.saveResume(resume);
            applicationService.submitApplication(user.getUserId(), jobId, savedFileName, coverLetter);
            // adds a temporary success message
            redirectAttributes.addFlashAttribute("successMessage", "Application submitted successfully.");
            return "redirect:/applicant/applications";
        } catch (IOException exception) {
            resumeStorageService.deleteResume(savedFileName);
            model.addAttribute("errorMessage", "The resume could not be uploaded. Please try again.");
        } catch (RuntimeException exception) {
            resumeStorageService.deleteResume(savedFileName);
            model.addAttribute("errorMessage", exception.getMessage());
        }

        // if the submission failed send the form data
        model.addAttribute("user", user);
        model.addAttribute("job", job);
        model.addAttribute("coverLetter", coverLetter);
        return "apply"; // apply page
    }

    @GetMapping("/applicant/applications")
    public String myApplications(HttpSession session, Model model) {
        User user = requireApplicant(session);
        if (user == null) {
            return loginRedirect(session);
        }

        List<Application> applications = applicationService.getApplicationsByUser(user.getUserId());
        model.addAttribute("user", user);
        model.addAttribute("applications", applications);
        return "my-applications";
    }


    // Custom method no end point
    private User requireApplicant(HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");

        if (sessionUser == null) {
            return null;
        }

        if (!"APPLICANT".equalsIgnoreCase(sessionUser.getRole())) {
            return null;
        }

        try {
            return userService.getUserById(sessionUser.getUserId());
        } catch (RuntimeException exception) {
            session.invalidate();
            return null;
        }
    }

    private String loginRedirect(HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");

        if (sessionUser == null) {
            return "redirect:/login";
        }

        if ("ADMIN".equalsIgnoreCase(sessionUser.getRole())) {
            return "redirect:/admin/dashboard";
        }

        return "redirect:/login";
    }
}
