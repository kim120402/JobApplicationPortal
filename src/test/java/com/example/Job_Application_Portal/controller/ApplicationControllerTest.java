package com.example.Job_Application_Portal.controller;

import com.example.Job_Application_Portal.model.User;
import com.example.Job_Application_Portal.model.Job;
import com.example.Job_Application_Portal.exception.DuplicateResourceException;
import com.example.Job_Application_Portal.exception.ValidationException;
import com.example.Job_Application_Portal.service.ApplicationService;
import com.example.Job_Application_Portal.service.JobService;
import com.example.Job_Application_Portal.service.ResumeStorageService;
import com.example.Job_Application_Portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

class ApplicationControllerTest {
    private ApplicationService applicationService;
    private ResumeStorageService resumeStorageService;
    private JobService jobService;
    private UserService userService;
    private ApplicationController applicationController;

    @BeforeEach
    void setUp() {
        applicationService = mock(ApplicationService.class);
        jobService = mock(JobService.class);
        resumeStorageService = mock(ResumeStorageService.class);
        userService = mock(UserService.class);
        applicationController = new ApplicationController(
                applicationService,
                jobService,
                resumeStorageService,
                userService
        );
    }

    @Test
    void adminCannotSubmitApplication() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User admin = new User();
        admin.setUserId(1L);
        admin.setRole("ADMIN");
        session.setAttribute("loggedInUser", admin);

        String view = applicationController.submitApplication(
                2L,
                null,
                new MockMultipartFile("resume", "resume.pdf", "application/pdf", "content".getBytes()),
                session,
                new ConcurrentModel(),
                new RedirectAttributesModelMap()
        );

        assertThat(view).isEqualTo("redirect:/admin/dashboard");
        verify(resumeStorageService, never()).saveResume(org.mockito.ArgumentMatchers.any());
        verify(applicationService, never()).submitApplication(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void missingSessionRedirectsToLogin() {
        String view = applicationController.submitApplication(
                2L,
                "cover",
                new MockMultipartFile("resume", "resume.pdf", "application/pdf", "content".getBytes()),
                new MockHttpSession(),
                new ConcurrentModel(),
                new RedirectAttributesModelMap()
        );

        assertThat(view).isEqualTo("redirect:/login");
    }

    @Test
    void validPdfApplicationSucceeds() throws Exception {
        MockHttpSession session = applicantSession();
        Job job = activeJob();
        when(userService.getUserById(1L)).thenReturn((User) session.getAttribute("loggedInUser"));
        when(jobService.getJobById(2L)).thenReturn(job);
        when(resumeStorageService.saveResume(any())).thenReturn("resume.pdf");

        String view = applicationController.submitApplication(
                2L,
                "I am interested.",
                new MockMultipartFile("resume", "resume.pdf", "application/pdf", "content".getBytes()),
                session,
                new ConcurrentModel(),
                new RedirectAttributesModelMap()
        );

        assertThat(view).isEqualTo("redirect:/applicant/applications");
        verify(applicationService).submitApplication(1L, 2L, "resume.pdf", "I am interested.");
    }

    @Test
    void validDocxApplicationSucceeds() throws Exception {
        MockHttpSession session = applicantSession();
        when(userService.getUserById(1L)).thenReturn((User) session.getAttribute("loggedInUser"));
        when(jobService.getJobById(2L)).thenReturn(activeJob());
        when(resumeStorageService.saveResume(any())).thenReturn("resume.docx");

        String view = applicationController.submitApplication(
                2L,
                "cover",
                new MockMultipartFile(
                        "resume",
                        "resume.docx",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "content".getBytes()
                ),
                session,
                new ConcurrentModel(),
                new RedirectAttributesModelMap()
        );

        assertThat(view).isEqualTo("redirect:/applicant/applications");
        verify(applicationService).submitApplication(1L, 2L, "resume.docx", "cover");
    }

    @Test
    void emptyResumeReturnsApplyWithFriendlyError() throws Exception {
        MockHttpSession session = applicantSession();
        ConcurrentModel model = new ConcurrentModel();
        when(userService.getUserById(1L)).thenReturn((User) session.getAttribute("loggedInUser"));
        when(jobService.getJobById(2L)).thenReturn(activeJob());
        when(resumeStorageService.saveResume(any()))
                .thenThrow(new ValidationException("Please upload a resume file"));

        String view = applicationController.submitApplication(
                2L,
                "cover",
                new MockMultipartFile("resume", "resume.pdf", "application/pdf", new byte[0]),
                session,
                model,
                new RedirectAttributesModelMap()
        );

        assertThat(view).isEqualTo("apply");
        assertThat(model.getAttribute("job")).isNotNull();
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Please upload a resume file");
    }

    @Test
    void invalidFileTypeReturnsApplyWithFriendlyError() throws Exception {
        MockHttpSession session = applicantSession();
        ConcurrentModel model = new ConcurrentModel();
        when(userService.getUserById(1L)).thenReturn((User) session.getAttribute("loggedInUser"));
        when(jobService.getJobById(2L)).thenReturn(activeJob());
        when(resumeStorageService.saveResume(any()))
                .thenThrow(new ValidationException("Resume must be a PDF or DOCX file"));

        String view = applicationController.submitApplication(
                2L,
                "cover",
                new MockMultipartFile("resume", "resume.exe", "application/octet-stream", "bad".getBytes()),
                session,
                model,
                new RedirectAttributesModelMap()
        );

        assertThat(view).isEqualTo("apply");
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Resume must be a PDF or DOCX file");
    }

    @Test
    void closedJobReturnsApplyWithFriendlyError() {
        MockHttpSession session = applicantSession();
        Job job = activeJob();
        job.setStatus(JobService.STATUS_CLOSED);
        ConcurrentModel model = new ConcurrentModel();
        when(userService.getUserById(1L)).thenReturn((User) session.getAttribute("loggedInUser"));
        when(jobService.getJobById(2L)).thenReturn(job);

        String view = applicationController.submitApplication(
                2L,
                "cover",
                new MockMultipartFile("resume", "resume.pdf", "application/pdf", "content".getBytes()),
                session,
                model,
                new RedirectAttributesModelMap()
        );

        assertThat(view).isEqualTo("apply");
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Applications are only available for active jobs.");
    }

    @Test
    void duplicateApplicationReturnsApplyWithFriendlyErrorAndDeletesSavedFile() throws Exception {
        MockHttpSession session = applicantSession();
        ConcurrentModel model = new ConcurrentModel();
        when(userService.getUserById(1L)).thenReturn((User) session.getAttribute("loggedInUser"));
        when(jobService.getJobById(2L)).thenReturn(activeJob());
        when(resumeStorageService.saveResume(any())).thenReturn("resume.pdf");
        when(applicationService.submitApplication(1L, 2L, "resume.pdf", "cover"))
                .thenThrow(new DuplicateResourceException("User has already applied for this job"));

        String view = applicationController.submitApplication(
                2L,
                "cover",
                new MockMultipartFile("resume", "resume.pdf", "application/pdf", "content".getBytes()),
                session,
                model,
                new RedirectAttributesModelMap()
        );

        assertThat(view).isEqualTo("apply");
        assertThat(model.getAttribute("errorMessage")).isEqualTo("User has already applied for this job");
        verify(resumeStorageService).deleteResume("resume.pdf");
    }

    @Test
    void successfulSubmissionAddsSuccessFlashMessage() throws Exception {
        MockHttpSession session = applicantSession();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        when(userService.getUserById(1L)).thenReturn((User) session.getAttribute("loggedInUser"));
        when(jobService.getJobById(2L)).thenReturn(activeJob());
        when(resumeStorageService.saveResume(any())).thenReturn("resume.pdf");

        String view = applicationController.submitApplication(
                2L,
                "cover",
                new MockMultipartFile("resume", "resume.pdf", "application/pdf", "content".getBytes()),
                session,
                new ConcurrentModel(),
                redirectAttributes
        );

        assertThat(view).isEqualTo("redirect:/applicant/applications");
        assertThat(redirectAttributes.getFlashAttributes().get("successMessage"))
                .isEqualTo("Application submitted successfully.");
    }

    @Test
    void myApplicationsPreservesApplicantSession() {
        MockHttpSession session = applicantSession();
        when(userService.getUserById(1L)).thenReturn((User) session.getAttribute("loggedInUser"));
        when(applicationService.getApplicationsByUser(1L)).thenReturn(List.of());
        ConcurrentModel model = new ConcurrentModel();

        String view = applicationController.myApplications(session, model);

        assertThat(view).isEqualTo("my-applications");
        assertThat(model.getAttribute("applications")).isEqualTo(List.of());
        assertThat(session.getAttribute("loggedInUser")).isNotNull();
        assertThat(session.isInvalid()).isFalse();
    }

    @Test
    void myApplicationsLoadsOnlyLoggedInApplicantsApplications() {
        MockHttpSession session = applicantSession();
        when(userService.getUserById(1L)).thenReturn((User) session.getAttribute("loggedInUser"));
        when(applicationService.getApplicationsByUser(1L)).thenReturn(List.of(new com.example.Job_Application_Portal.model.Application()));

        String view = applicationController.myApplications(session, new ConcurrentModel());

        assertThat(view).isEqualTo("my-applications");
        verify(applicationService).getApplicationsByUser(1L);
    }

    @Test
    void adminCannotOpenApplicantApplicationsPage() {
        MockHttpSession session = new MockHttpSession();
        User admin = new User();
        admin.setUserId(2L);
        admin.setRole("ADMIN");
        session.setAttribute("loggedInUser", admin);

        String view = applicationController.myApplications(session, new ConcurrentModel());

        assertThat(view).isEqualTo("redirect:/admin/dashboard");
        verify(applicationService, never()).getApplicationsByUser(any());
    }

    private MockHttpSession applicantSession() {
        MockHttpSession session = new MockHttpSession();
        User applicant = new User();
        applicant.setUserId(1L);
        applicant.setRole("APPLICANT");
        applicant.setFullName("Applicant User");
        session.setAttribute("loggedInUser", applicant);
        return session;
    }

    private Job activeJob() {
        Job job = new Job();
        job.setJobId(2L);
        job.setStatus(JobService.STATUS_ACTIVE);
        return job;
    }
}
