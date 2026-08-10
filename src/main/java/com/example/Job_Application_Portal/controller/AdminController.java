package com.example.Job_Application_Portal.controller;

import com.example.Job_Application_Portal.dto.DashboardStatisticsResponse;
import com.example.Job_Application_Portal.exception.ValidationException;
import com.example.Job_Application_Portal.model.Application;
import com.example.Job_Application_Portal.model.Job;
import com.example.Job_Application_Portal.model.User;
import com.example.Job_Application_Portal.service.ApplicationService;
import com.example.Job_Application_Portal.service.DashboardService;
import com.example.Job_Application_Portal.service.JobService;
import com.example.Job_Application_Portal.service.ResumeStorageService;
import com.example.Job_Application_Portal.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.MalformedURLException;
import java.util.List;

@Controller
public class AdminController {
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final UserService userService;
    private final ResumeStorageService resumeStorageService;
    private final DashboardService dashboardService;

    public AdminController(
            JobService jobService,
            ApplicationService applicationService,
            UserService userService,
            ResumeStorageService resumeStorageService,
            DashboardService dashboardService
    ) {
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.userService = userService;
        this.resumeStorageService = resumeStorageService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        User admin = requireAdmin(session);
        if (admin == null) {
            return adminRedirect(session);
        }

        model.addAttribute("user", admin);
        addDashboardStats(model);
        return "admin-dashboard";
    }

    @GetMapping("/admin/jobs")
    public String manageJobs(HttpSession session, Model model) {
        if (requireAdmin(session) == null) {
            return adminRedirect(session);
        }

        model.addAttribute("jobs", jobService.getAllJobs());
        return "manage-jobs";
    }

    @GetMapping("/admin/jobs/new")
    public String showCreateJobForm(HttpSession session, Model model) {
        if (requireAdmin(session) == null) {
            return adminRedirect(session);
        }

        model.addAttribute("job", new Job());
        return "create-job";
    }

    @PostMapping("/admin/jobs/new")
    public String createJob(
            @Valid @ModelAttribute("job") Job job,
            BindingResult result,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (requireAdmin(session) == null) {
            return adminRedirect(session);
        }

        if (result.hasErrors()) {
            return "create-job";
        }

        try {
            jobService.createJob(job);
            redirectAttributes.addFlashAttribute("successMessage", "Job created successfully.");
            return "redirect:/admin/jobs";
        } catch (RuntimeException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            return "create-job";
        }
    }

    @GetMapping("/admin/jobs/{jobId}/edit")
    public String showEditJobForm(
            @PathVariable Long jobId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (requireAdmin(session) == null) {
            return adminRedirect(session);
        }

        try {
            model.addAttribute("job", jobService.getJobById(jobId));
            return "edit-job";
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Job not found.");
            return "redirect:/admin/jobs";
        }
    }

    @PostMapping("/admin/jobs/{jobId}/edit")
    public String editJob(
            @PathVariable Long jobId,
            @Valid @ModelAttribute("job") Job job,
            BindingResult result,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (requireAdmin(session) == null) {
            return adminRedirect(session);
        }

        if (result.hasErrors()) {
            job.setJobId(jobId);
            return "edit-job";
        }

        try {
            job.setJobId(jobId);
            job.setPostedDate(null);
            jobService.updateJob(jobId, job);
            redirectAttributes.addFlashAttribute("successMessage", "Job updated successfully.");
            return "redirect:/admin/jobs";
        } catch (RuntimeException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            return "edit-job";
        }
    }

    @PostMapping("/admin/jobs/{jobId}/delete")
    public String deleteJob(
            @PathVariable Long jobId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (requireAdmin(session) == null) {
            return adminRedirect(session);
        }

        try {
            jobService.deleteJob(jobId);
            redirectAttributes.addFlashAttribute("successMessage", "Job deleted successfully.");
        } catch (com.example.Job_Application_Portal.exception.JobDeletionBlockedException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "This job cannot be deleted because applications are linked to it. Change the status to CLOSED instead."
            );
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Job could not be deleted.");
        }

        return "redirect:/admin/jobs";
    }

    @GetMapping("/admin/applications")
    public String viewApplications(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long jobId,
            HttpSession session,
            Model model
    ) {
        if (requireAdmin(session) == null) {
            return adminRedirect(session);
        }

        try {
            List<Application> applications = findApplications(status, jobId);
            model.addAttribute("applications", applications);
            model.addAttribute("status", status);
            model.addAttribute("jobId", jobId);
        } catch (RuntimeException exception) {
            model.addAttribute("applications", List.of());
            model.addAttribute("errorMessage", exception.getMessage());
        }

        return "view-applications";
    }

    @GetMapping("/admin/applications/{applicationId}")
    public String applicationDetails(
            @PathVariable Long applicationId,
            HttpSession session,
            Model model
    ) {
        if (requireAdmin(session) == null) {
            return adminRedirect(session);
        }

        try {
            model.addAttribute("jobApplication", applicationService.getApplicationById(applicationId));
        } catch (RuntimeException exception) {
            model.addAttribute("errorMessage", "Application not found.");
        }

        return "application-details";
    }

    @GetMapping("/admin/applications/{applicationId}/resume")
    public ResponseEntity<?> downloadResume(@PathVariable Long applicationId, HttpSession session) {

        // checks that the logged-in user is actually an admin
        if (requireAdmin(session) == null) {
            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, adminRedirect(session).replace("redirect:", ""))
                    .build();
        }

        try {
            // Then it gets the specific application:
            Application application = applicationService.getApplicationById(applicationId);
            // gets the resume filename from that application
            Resource resume = resumeStorageService.loadResume(application.getResumeFileName());
            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + application.getResumeFileName() + "\""
                    )
                    .body(resume); // send resume file back to the browser as a download
        } catch (MalformedURLException | RuntimeException exception) {
            return ResponseEntity.badRequest().body("Resume file is missing or cannot be downloaded.");
        }
    }

    @GetMapping("/admin/applications/{applicationId}/status")
    public String showUpdateStatusForm(
            @PathVariable Long applicationId,
            HttpSession session,
            Model model
    ) {
        if (requireAdmin(session) == null) {
            return adminRedirect(session);
        }

        try {
            model.addAttribute("jobApplication", applicationService.getApplicationById(applicationId));
            return "update-application-status";
        } catch (RuntimeException exception) {
            model.addAttribute("errorMessage", "Application not found.");
            return "application-details";
        }
    }

    @PostMapping("/admin/applications/{applicationId}/status")
    public String updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestParam String status,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (requireAdmin(session) == null) {
            return adminRedirect(session);
        }

        try {
            applicationService.updateApplicationStatus(applicationId, status);
            redirectAttributes.addFlashAttribute("successMessage", "Application status updated successfully.");
            return "redirect:/admin/applications/" + applicationId;
        } catch (ValidationException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("jobApplication", applicationService.getApplicationById(applicationId));
            return "update-application-status";
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Application not found.");
            return "redirect:/admin/applications";
        }
    }

    private void addDashboardStats(Model model) {
        DashboardStatisticsResponse statistics = dashboardService.getDashboardStatistics();
        model.addAttribute("totalJobs", statistics.totalJobs());
        model.addAttribute("activeJobs", statistics.activeJobs());
        model.addAttribute("closedJobs", statistics.closedJobs());
        model.addAttribute("totalApplicants", statistics.totalApplicants());
        model.addAttribute("totalApplications", statistics.totalApplications());
        model.addAttribute("underReviewApplications", statistics.underReviewApplications());
        model.addAttribute("interviewApplications", statistics.interviewApplications());
        model.addAttribute("hiredApplications", statistics.hiredApplications());
    }

    private List<Application> findApplications(String status, Long jobId) {
        if (jobId != null) {
            List<Application> applications = applicationService.getApplicationsByJob(jobId);
            if (status == null || status.isBlank()) {
                return applications;
            }

            String normalizedStatus = status.trim().toUpperCase();
            return applications.stream()
                    .filter(application -> normalizedStatus.equalsIgnoreCase(application.getApplicationStatus()))
                    .toList();
        }

        if (status != null && !status.isBlank()) {
            return applicationService.getApplicationsByStatus(status);
        }

        return applicationService.getAllApplications();
    }

    private User requireAdmin(HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");

        if (sessionUser == null || !"ADMIN".equalsIgnoreCase(sessionUser.getRole())) {
            return null;
        }

        try {
            return userService.getUserById(sessionUser.getUserId());
        } catch (RuntimeException exception) {
            session.invalidate();
            return null;
        }
    }

    private String adminRedirect(HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");

        if (sessionUser != null && "APPLICANT".equalsIgnoreCase(sessionUser.getRole())) {
            return "redirect:/applicant/dashboard";
        }

        return "redirect:/login";
    }
}
