package com.example.Job_Application_Portal.rest;

import com.example.Job_Application_Portal.dto.ApplicationResponse;
import com.example.Job_Application_Portal.dto.ApplicationStatusRequest;
import com.example.Job_Application_Portal.exception.ForbiddenAccessException;
import com.example.Job_Application_Portal.model.Application;
import com.example.Job_Application_Portal.model.User;
import com.example.Job_Application_Portal.service.ApplicationService;
import com.example.Job_Application_Portal.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApplicationRestController {
    private final ApplicationService applicationService;
    private final UserService userService;
    private final SessionAccessHelper sessionAccessHelper;

    public ApplicationRestController(
            ApplicationService applicationService,
            UserService userService,
            SessionAccessHelper sessionAccessHelper
    ) {
        this.applicationService = applicationService;
        this.userService = userService;
        this.sessionAccessHelper = sessionAccessHelper;
    }

    @GetMapping("/applications")
    public List<ApplicationResponse> getApplications(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long jobId,
            HttpSession session
    ) {
        sessionAccessHelper.requireAdmin(session);
        return findApplications(status, jobId).stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    @GetMapping("/applications/{applicationId}")
    public ApplicationResponse getApplication(@PathVariable Long applicationId, HttpSession session) {
        User user = sessionAccessHelper.requireLoggedInUser(session);
        Application application = applicationService.getApplicationById(applicationId);

        if ("APPLICANT".equalsIgnoreCase(user.getRole())
                && !application.getUser().getUserId().equals(user.getUserId())) {
            throw new ForbiddenAccessException("You can only view your own applications");
        }

        return ApplicationResponse.from(application);
    }

    @GetMapping("/applications/user/{userId}")
    public List<ApplicationResponse> getUserApplications(@PathVariable Long userId, HttpSession session) {
        User sessionUser = sessionAccessHelper.requireLoggedInUser(session);
        userService.getUserById(userId);

        if ("APPLICANT".equalsIgnoreCase(sessionUser.getRole()) && !sessionUser.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("You can only view your own applications");
        }

        return applicationService.getApplicationsByUser(userId).stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    @PutMapping("/applications/{applicationId}/status")
    public ApplicationResponse updateApplicationStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationStatusRequest request,
            HttpSession session
    ) {
        sessionAccessHelper.requireAdmin(session);
        return ApplicationResponse.from(applicationService.updateApplicationStatus(applicationId, request.status()));
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
}
