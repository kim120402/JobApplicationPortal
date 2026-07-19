package com.example.Job_Application_Portal.service;

import com.example.Job_Application_Portal.exception.DuplicateResourceException;
import com.example.Job_Application_Portal.exception.InvalidOperationException;
import com.example.Job_Application_Portal.exception.ResourceNotFoundException;
import com.example.Job_Application_Portal.exception.ValidationException;
import com.example.Job_Application_Portal.model.Application;
import com.example.Job_Application_Portal.model.Job;
import com.example.Job_Application_Portal.model.User;
import com.example.Job_Application_Portal.repository.ApplicationRepository;
import com.example.Job_Application_Portal.repository.JobRepository;
import com.example.Job_Application_Portal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplicationServiceTest {
    private ApplicationRepository applicationRepository;
    private UserRepository userRepository;
    private JobRepository jobRepository;
    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(ApplicationRepository.class);
        userRepository = mock(UserRepository.class);
        jobRepository = mock(JobRepository.class);
        applicationService = new ApplicationService(applicationRepository, userRepository, jobRepository);
    }

    @Test
    void submitApplicationCreatesSubmittedApplication() {
        User user = applicant();
        Job job = activeJob();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jobRepository.findById(2L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByUserUserIdAndJobJobId(1L, 2L)).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Application application = applicationService.submitApplication(
                1L,
                2L,
                " resume.pdf ",
                " I am interested. "
        );

        assertThat(application.getUser()).isSameAs(user);
        assertThat(application.getJob()).isSameAs(job);
        assertThat(application.getResumeFileName()).isEqualTo("resume.pdf");
        assertThat(application.getCoverLetter()).isEqualTo("I am interested.");
        assertThat(application.getApplicationStatus()).isEqualTo(ApplicationService.STATUS_SUBMITTED);
        assertThat(application.getAppliedDate()).isNotNull();
    }

    @Test
    void submitApplicationRejectsDuplicateApplication() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(applicant()));
        when(jobRepository.findById(2L)).thenReturn(Optional.of(activeJob()));
        when(applicationRepository.existsByUserUserIdAndJobJobId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> applicationService.submitApplication(1L, 2L, "resume.pdf", null))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("User has already applied for this job");

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void submitApplicationRejectsClosedJob() {
        Job job = activeJob();
        job.setStatus(JobService.STATUS_CLOSED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(applicant()));
        when(jobRepository.findById(2L)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> applicationService.submitApplication(1L, 2L, "resume.pdf", null))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessage("Applications can only be submitted for active jobs");
    }

    @Test
    void submitApplicationRejectsMissingResume() {
        assertThatThrownBy(() -> applicationService.submitApplication(1L, 2L, " ", null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Resume file name is required");
    }

    @Test
    void updateApplicationStatusRejectsUnknownApplication() {
        when(applicationRepository.findById(50L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.updateApplicationStatus(50L, ApplicationService.STATUS_REVIEWED))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Application not found");
    }

    @Test
    void updateApplicationStatusSavesNormalizedStatus() {
        Application application = new Application();
        when(applicationRepository.findById(5L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Application updatedApplication = applicationService.updateApplicationStatus(5L, " reviewed ");

        assertThat(updatedApplication.getApplicationStatus()).isEqualTo(ApplicationService.STATUS_REVIEWED);
    }

    private User applicant() {
        User user = new User();
        user.setUserId(1L);
        user.setRole("APPLICANT");
        return user;
    }

    private Job activeJob() {
        Job job = new Job();
        job.setJobId(2L);
        job.setStatus(JobService.STATUS_ACTIVE);
        return job;
    }
}
