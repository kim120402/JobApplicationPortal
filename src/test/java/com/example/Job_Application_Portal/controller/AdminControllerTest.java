package com.example.Job_Application_Portal.controller;

import com.example.Job_Application_Portal.exception.ValidationException;
import com.example.Job_Application_Portal.exception.JobDeletionBlockedException;
import com.example.Job_Application_Portal.model.Application;
import com.example.Job_Application_Portal.model.Job;
import com.example.Job_Application_Portal.model.User;
import com.example.Job_Application_Portal.service.ApplicationService;
import com.example.Job_Application_Portal.service.DashboardService;
import com.example.Job_Application_Portal.service.JobService;
import com.example.Job_Application_Portal.service.ResumeStorageService;
import com.example.Job_Application_Portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminControllerTest {
    private JobService jobService;
    private ApplicationService applicationService;
    private UserService userService;
    private ResumeStorageService resumeStorageService;
    private DashboardService dashboardService;
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        jobService = mock(JobService.class);
        applicationService = mock(ApplicationService.class);
        userService = mock(UserService.class);
        resumeStorageService = mock(ResumeStorageService.class);
        dashboardService = mock(DashboardService.class);
        adminController = new AdminController(jobService, applicationService, userService, resumeStorageService, dashboardService);
        when(dashboardService.getDashboardStatistics()).thenReturn(new com.example.Job_Application_Portal.dto.DashboardStatisticsResponse(
                0, 0, 0, 0, 0, 0, 0, 0
        ));
    }

    @Test
    void unauthenticatedUserCannotAccessAdminDashboard() {
        String view = adminController.adminDashboard(new MockHttpSession(), new ConcurrentModel());

        assertThat(view).isEqualTo("redirect:/login");
    }

    @Test
    void applicantCannotAccessAdminDashboard() {
        String view = adminController.adminDashboard(applicantSession(), new ConcurrentModel());

        assertThat(view).isEqualTo("redirect:/applicant/dashboard");
    }

    @Test
    void adminCanAccessAdminDashboard() {
        MockHttpSession session = adminSession();
        User admin = admin();
        when(userService.getUserById(1L)).thenReturn(admin);
        when(dashboardService.getDashboardStatistics()).thenReturn(new com.example.Job_Application_Portal.dto.DashboardStatisticsResponse(
                4, 3, 1, 8, 12, 5, 2, 1
        ));
        ConcurrentModel model = new ConcurrentModel();

        String view = adminController.adminDashboard(session, model);

        assertThat(view).isEqualTo("admin-dashboard");
        assertThat(model.getAttribute("user")).isSameAs(admin);
    }

    @Test
    void dashboardStatisticsReturnCorrectValues() {
        MockHttpSession session = adminSession();
        when(userService.getUserById(1L)).thenReturn(admin());
        when(dashboardService.getDashboardStatistics()).thenReturn(new com.example.Job_Application_Portal.dto.DashboardStatisticsResponse(
                6, 4, 2, 10, 15, 7, 3, 2
        ));
        ConcurrentModel model = new ConcurrentModel();

        adminController.adminDashboard(session, model);

        assertThat(model.getAttribute("totalJobs")).isEqualTo(6L);
        assertThat(model.getAttribute("activeJobs")).isEqualTo(4L);
        assertThat(model.getAttribute("closedJobs")).isEqualTo(2L);
        assertThat(model.getAttribute("totalApplicants")).isEqualTo(10L);
        assertThat(model.getAttribute("totalApplications")).isEqualTo(15L);
        assertThat(model.getAttribute("underReviewApplications")).isEqualTo(7L);
        assertThat(model.getAttribute("interviewApplications")).isEqualTo(3L);
        assertThat(model.getAttribute("hiredApplications")).isEqualTo(2L);
    }

    @Test
    void adminCanCreateJob() {
        when(userService.getUserById(1L)).thenReturn(admin());
        Job job = validJob();
        BindingResult result = new BeanPropertyBindingResult(job, "job");

        String view = adminController.createJob(
                job,
                result,
                adminSession(),
                new ConcurrentModel(),
                new RedirectAttributesModelMap()
        );

        assertThat(view).isEqualTo("redirect:/admin/jobs");
        verify(jobService).createJob(job);
    }

    @Test
    void invalidJobDataIsRejected() {
        when(userService.getUserById(1L)).thenReturn(admin());
        Job job = validJob();
        BindingResult result = new BeanPropertyBindingResult(job, "job");
        result.rejectValue("title", "required", "Job title is required");

        String view = adminController.createJob(
                job,
                result,
                adminSession(),
                new ConcurrentModel(),
                new RedirectAttributesModelMap()
        );

        assertThat(view).isEqualTo("create-job");
        verify(jobService, never()).createJob(any(Job.class));
    }

    @Test
    void adminCanEditExistingJob() {
        when(userService.getUserById(1L)).thenReturn(admin());
        Job job = validJob();
        BindingResult result = new BeanPropertyBindingResult(job, "job");

        String view = adminController.editJob(
                2L,
                job,
                result,
                adminSession(),
                new ConcurrentModel(),
                new RedirectAttributesModelMap()
        );

        assertThat(view).isEqualTo("redirect:/admin/jobs");
        verify(jobService).updateJob(2L, job);
    }

    @Test
    void editingDoesNotCreateDuplicateJob() {
        when(userService.getUserById(1L)).thenReturn(admin());
        Job job = validJob();

        adminController.editJob(
                2L,
                job,
                new BeanPropertyBindingResult(job, "job"),
                adminSession(),
                new ConcurrentModel(),
                new RedirectAttributesModelMap()
        );

        verify(jobService, never()).createJob(any(Job.class));
        verify(jobService).updateJob(2L, job);
    }

    @Test
    void adminCanDeleteJobWithNoApplications() {
        when(userService.getUserById(1L)).thenReturn(admin());

        String view = adminController.deleteJob(2L, adminSession(), new RedirectAttributesModelMap());

        assertThat(view).isEqualTo("redirect:/admin/jobs");
        verify(jobService).deleteJob(2L);
    }

    @Test
    void jobWithApplicationsCannotBeDeleted() {
        when(userService.getUserById(1L)).thenReturn(admin());
        org.mockito.Mockito.doThrow(new JobDeletionBlockedException("blocked"))
                .when(jobService).deleteJob(2L);

        String view = adminController.deleteJob(2L, adminSession(), new RedirectAttributesModelMap());

        assertThat(view).isEqualTo("redirect:/admin/jobs");
        verify(jobService).deleteJob(2L);
    }

    @Test
    void adminCanViewAllApplications() {
        when(userService.getUserById(1L)).thenReturn(admin());
        when(applicationService.getAllApplications()).thenReturn(List.of(new Application()));
        ConcurrentModel model = new ConcurrentModel();

        String view = adminController.viewApplications(null, null, adminSession(), model);

        assertThat(view).isEqualTo("view-applications");
        assertThat((List<?>) model.getAttribute("applications")).hasSize(1);
    }

    @Test
    void emptyApplicationListRendersSuccessfully() {
        MockHttpSession session = adminSession();
        when(userService.getUserById(1L)).thenReturn(admin());
        when(applicationService.getAllApplications()).thenReturn(List.of());
        ConcurrentModel model = new ConcurrentModel();

        String view = adminController.viewApplications(null, null, session, model);

        assertThat(view).isEqualTo("view-applications");
        assertThat(model.getAttribute("applications")).isEqualTo(List.of());
        assertThat(session.getAttribute("loggedInUser")).isNotNull();
        assertThat(session.isInvalid()).isFalse();
    }

    @Test
    void applicantCannotViewAllApplications() {
        String view = adminController.viewApplications(null, null, applicantSession(), new ConcurrentModel());

        assertThat(view).isEqualTo("redirect:/applicant/dashboard");
    }

    @Test
    void adminCanViewApplicationDetails() {
        when(userService.getUserById(1L)).thenReturn(admin());
        Application application = new Application();
        when(applicationService.getApplicationById(3L)).thenReturn(application);
        ConcurrentModel model = new ConcurrentModel();

        String view = adminController.applicationDetails(3L, adminSession(), model);

        assertThat(view).isEqualTo("application-details");
        assertThat(model.getAttribute("jobApplication")).isSameAs(application);
    }

    @Test
    void adminCanUpdateApplicationStatus() {
        when(userService.getUserById(1L)).thenReturn(admin());

        String view = adminController.updateApplicationStatus(
                3L,
                ApplicationService.STATUS_INTERVIEW,
                adminSession(),
                new ConcurrentModel(),
                new RedirectAttributesModelMap()
        );

        assertThat(view).isEqualTo("redirect:/admin/applications/3");
        verify(applicationService).updateApplicationStatus(3L, ApplicationService.STATUS_INTERVIEW);
    }

    @Test
    void invalidApplicationStatusIsRejected() {
        when(userService.getUserById(1L)).thenReturn(admin());
        Application application = new Application();
        when(applicationService.getApplicationById(3L)).thenReturn(application);
        when(applicationService.updateApplicationStatus(3L, "INVALID"))
                .thenThrow(new ValidationException("Application status must be APPLIED, UNDER_REVIEW, INTERVIEW, REJECTED, or HIRED"));

        String view = adminController.updateApplicationStatus(
                3L,
                "INVALID",
                adminSession(),
                new ConcurrentModel(),
                new RedirectAttributesModelMap()
        );

        assertThat(view).isEqualTo("update-application-status");
    }

    @Test
    void resumeDownloadDoesNotAllowUnauthorizedAccess() {
        assertThat(adminController.downloadResume(3L, applicantSession()).getStatusCode().value()).isEqualTo(302);
    }

    @Test
    void resumeDownloadHandlesMissingFileSafely() throws Exception {
        when(userService.getUserById(1L)).thenReturn(admin());
        Application application = new Application();
        application.setResumeFileName("missing.pdf");
        when(applicationService.getApplicationById(3L)).thenReturn(application);
        when(resumeStorageService.loadResume("missing.pdf"))
                .thenThrow(new ValidationException("Resume file is missing"));

        assertThat(adminController.downloadResume(3L, adminSession()).getStatusCode().value()).isEqualTo(400);
    }

    private MockHttpSession adminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", admin());
        return session;
    }

    private MockHttpSession applicantSession() {
        MockHttpSession session = new MockHttpSession();
        User applicant = new User();
        applicant.setUserId(9L);
        applicant.setRole("APPLICANT");
        session.setAttribute("loggedInUser", applicant);
        return session;
    }

    private User admin() {
        User user = new User();
        user.setUserId(1L);
        user.setFullName("Admin User");
        user.setRole("ADMIN");
        return user;
    }

    private Job validJob() {
        Job job = new Job();
        job.setTitle("Developer Intern");
        job.setCompanyName("Job Portal");
        job.setLocation("Toronto");
        job.setCategory("Technology");
        job.setEmploymentType("CO_OP");
        job.setDescription("Build applications");
        job.setRequirements("Java");
        job.setStatus(JobService.STATUS_ACTIVE);
        return job;
    }
}
