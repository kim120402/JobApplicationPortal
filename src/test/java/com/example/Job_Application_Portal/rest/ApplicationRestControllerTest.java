package com.example.Job_Application_Portal.rest;

import com.example.Job_Application_Portal.exception.ForbiddenAccessException;
import com.example.Job_Application_Portal.exception.ResourceNotFoundException;
import com.example.Job_Application_Portal.exception.UnauthorizedAccessException;
import com.example.Job_Application_Portal.exception.ValidationException;
import com.example.Job_Application_Portal.model.Application;
import com.example.Job_Application_Portal.model.Job;
import com.example.Job_Application_Portal.model.User;
import com.example.Job_Application_Portal.service.ApplicationService;
import com.example.Job_Application_Portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApplicationRestControllerTest {
    private ApplicationService applicationService;
    private UserService userService;
    private SessionAccessHelper sessionAccessHelper;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        applicationService = mock(ApplicationService.class);
        userService = mock(UserService.class);
        sessionAccessHelper = mock(SessionAccessHelper.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new ApplicationRestController(applicationService, userService, sessionAccessHelper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void unauthenticatedApplicationListReturnsUnauthorized() throws Exception {
        when(sessionAccessHelper.requireAdmin(any())).thenThrow(new UnauthorizedAccessException("Login is required"));

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void applicantCannotListAllApplications() throws Exception {
        when(sessionAccessHelper.requireAdmin(any())).thenThrow(new ForbiddenAccessException("Admin access is required"));

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListAllApplications() throws Exception {
        when(applicationService.getAllApplications()).thenReturn(List.of(application(1L, 2L)));

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationId").value(1))
                .andExpect(content().string(not(containsString("password"))))
                .andExpect(content().string(not(containsString("uploads"))));
    }

    @Test
    void applicantCanAccessOwnApplication() throws Exception {
        User user = user(2L, "APPLICANT");
        when(sessionAccessHelper.requireLoggedInUser(any())).thenReturn(user);
        when(applicationService.getApplicationById(1L)).thenReturn(application(1L, 2L));

        mockMvc.perform(get("/api/applications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicantId").value(2));
    }

    @Test
    void applicantCannotAccessOtherApplication() throws Exception {
        when(sessionAccessHelper.requireLoggedInUser(any())).thenReturn(user(3L, "APPLICANT"));
        when(applicationService.getApplicationById(1L)).thenReturn(application(1L, 2L));

        mockMvc.perform(get("/api/applications/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanUpdateApplicationStatus() throws Exception {
        when(applicationService.updateApplicationStatus(1L, "INTERVIEW")).thenReturn(application(1L, 2L));

        mockMvc.perform(put("/api/applications/1/status")
                        .contentType("application/json")
                        .content("{\"status\":\"INTERVIEW\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void applicantCannotUpdateApplicationStatus() throws Exception {
        when(sessionAccessHelper.requireAdmin(any())).thenThrow(new ForbiddenAccessException("Admin access is required"));

        mockMvc.perform(put("/api/applications/1/status")
                        .contentType("application/json")
                        .content("{\"status\":\"INTERVIEW\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidStatusReturnsBadRequest() throws Exception {
        when(applicationService.updateApplicationStatus(1L, "BAD")).thenThrow(new ValidationException("Invalid status"));

        mockMvc.perform(put("/api/applications/1/status")
                        .contentType("application/json")
                        .content("{\"status\":\"BAD\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingApplicationReturnsNotFound() throws Exception {
        when(sessionAccessHelper.requireLoggedInUser(any())).thenReturn(user(1L, "ADMIN"));
        when(applicationService.getApplicationById(99L)).thenThrow(new ResourceNotFoundException("Application not found"));

        mockMvc.perform(get("/api/applications/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void applicantCannotRequestAnotherUsersApplications() throws Exception {
        when(sessionAccessHelper.requireLoggedInUser(any())).thenReturn(user(2L, "APPLICANT"));
        when(userService.getUserById(3L)).thenReturn(user(3L, "APPLICANT"));

        mockMvc.perform(get("/api/applications/user/3"))
                .andExpect(status().isForbidden());
    }

    private Application application(Long applicationId, Long userId) {
        Application application = new Application();
        application.setApplicationId(applicationId);
        application.setUser(user(userId, "APPLICANT"));
        Job job = new Job();
        job.setJobId(5L);
        job.setTitle("Developer");
        job.setCompanyName("Company");
        job.setLocation("Toronto");
        job.setEmploymentType("CO_OP");
        application.setJob(job);
        application.setResumeFileName("resume.pdf");
        application.setCoverLetter("Hello");
        application.setApplicationStatus("APPLIED");
        application.setAppliedDate(LocalDateTime.now());
        return application;
    }

    private User user(Long id, String role) {
        User user = new User();
        user.setUserId(id);
        user.setFullName("User " + id);
        user.setEmail("user" + id + "@example.com");
        user.setRole(role);
        user.setPassword("secret");
        return user;
    }
}
