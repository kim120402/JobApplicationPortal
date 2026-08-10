package com.example.Job_Application_Portal.service;

import com.example.Job_Application_Portal.exception.ResourceNotFoundException;
import com.example.Job_Application_Portal.exception.ValidationException;
import com.example.Job_Application_Portal.model.Job;
import com.example.Job_Application_Portal.repository.ApplicationRepository;
import com.example.Job_Application_Portal.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobServiceTest {
    private JobRepository jobRepository;
    private ApplicationRepository applicationRepository;
    private JobService jobService;

    @BeforeEach
    void setUp() {
        jobRepository = mock(JobRepository.class);
        applicationRepository = mock(ApplicationRepository.class);
        jobService = new JobService(jobRepository, applicationRepository);
    }

    @Test
    void createJobDefaultsStatusAndPostedDate() {
        Job job = validJob();
        job.setStatus(null);
        job.setPostedDate(null);
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Job savedJob = jobService.createJob(job);

        assertThat(savedJob.getTitle()).isEqualTo("Software Developer Intern");
        assertThat(savedJob.getStatus()).isEqualTo(JobService.STATUS_ACTIVE);
        assertThat(savedJob.getPostedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void createJobRejectsMissingTitle() {
        Job job = validJob();
        job.setTitle(" ");

        assertThatThrownBy(() -> jobService.createJob(job))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Job title is required");

        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    void getJobByIdRejectsUnknownJob() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.getJobById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Job not found");
    }

    @Test
    void searchActiveJobsByTitleUsesActiveStatus() {
        Job job = validJob();
        when(jobRepository.findByTitleContainingIgnoreCaseAndStatusIgnoreCaseOrderByPostedDateDesc(
                "developer",
                JobService.STATUS_ACTIVE
        )).thenReturn(List.of(job));

        List<Job> jobs = jobService.searchActiveJobsByTitle(" developer ");

        assertThat(jobs).containsExactly(job);
    }

    @Test
    void closeJobMarksJobClosed() {
        Job job = validJob();
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Job closedJob = jobService.closeJob(1L);

        assertThat(closedJob.getStatus()).isEqualTo(JobService.STATUS_CLOSED);
    }

    @Test
    void deleteJobDeletesExistingJob() {
        Job job = validJob();
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        jobService.deleteJob(1L);

        verify(jobRepository).delete(job);
    }

    @Test
    void deleteJobRejectsUnknownJob() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.deleteJob(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Job not found");

        verify(jobRepository, never()).delete(any(Job.class));
    }

    @Test
    void deleteJobRejectsMissingJobId() {
        assertThatThrownBy(() -> jobService.deleteJob(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Job id is required");

        verify(jobRepository, never()).delete(any(Job.class));
    }

    private Job validJob() {
        Job job = new Job();
        job.setTitle(" Software Developer Intern ");
        job.setCompanyName("Job Portal");
        job.setLocation("Toronto");
        job.setCategory("Technology");
        job.setEmploymentType("Co-op");
        job.setDescription("Build web applications");
        job.setRequirements("Java and Spring Boot");
        job.setStatus(JobService.STATUS_ACTIVE);
        job.setPostedDate(LocalDate.now());
        return job;
    }
}
