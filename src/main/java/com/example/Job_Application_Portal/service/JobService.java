package com.example.Job_Application_Portal.service;

import com.example.Job_Application_Portal.exception.JobDeletionBlockedException;
import com.example.Job_Application_Portal.exception.ResourceNotFoundException;
import com.example.Job_Application_Portal.exception.ValidationException;
import com.example.Job_Application_Portal.model.Job;
import com.example.Job_Application_Portal.repository.ApplicationRepository;
import com.example.Job_Application_Portal.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class JobService {
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_CLOSED = "CLOSED";

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public JobService(JobRepository jobRepository, ApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    public Job createJob(Job job) {
        validateJob(job);

        job.setTitle(job.getTitle().trim());
        job.setCompanyName(job.getCompanyName().trim());
        job.setLocation(job.getLocation().trim());
        job.setCategory(job.getCategory().trim());
        job.setEmploymentType(job.getEmploymentType().trim());
        job.setDescription(job.getDescription().trim());
        job.setRequirements(job.getRequirements().trim());
        job.setStatus(normalizeStatus(job.getStatus()));
        if (job.getPostedDate() == null) {
            job.setPostedDate(LocalDate.now());
        }

        return jobRepository.save(job);
    }

    public Job updateJob(Long jobId, Job job) {
        if (jobId == null) {
            throw new ValidationException("Job id is required");
        }
        validateJob(job);

        Job existingJob = getJobById(jobId);
        existingJob.setTitle(job.getTitle().trim());
        existingJob.setCompanyName(job.getCompanyName().trim());
        existingJob.setLocation(job.getLocation().trim());
        existingJob.setCategory(job.getCategory().trim());
        existingJob.setEmploymentType(job.getEmploymentType().trim());
        existingJob.setDescription(job.getDescription().trim());
        existingJob.setRequirements(job.getRequirements().trim());
        existingJob.setStatus(normalizeStatus(job.getStatus()));
        if (job.getPostedDate() != null) {
            existingJob.setPostedDate(job.getPostedDate());
        }

        return jobRepository.save(existingJob);
    }

    public Job getJobById(Long jobId) {
        if (jobId == null) {
            throw new ValidationException("Job id is required");
        }

        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    public List<Job> getActiveJobs() {
        return jobRepository.findByStatusIgnoreCaseOrderByPostedDateDesc(STATUS_ACTIVE);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAllByOrderByPostedDateDesc();
    }

    public List<Job> getClosedJobs() {
        return jobRepository.findByStatusIgnoreCaseOrderByPostedDateDesc(STATUS_CLOSED);
    }

    public long countAllJobs() {
        return jobRepository.count();
    }

    public long countJobsByStatus(String status) {
        return jobRepository.countByStatusIgnoreCase(normalizeStatus(status));
    }

    public List<Job> searchActiveJobsByTitle(String title) {
        if (isBlank(title)) {
            throw new ValidationException("Job title search term is required");
        }

        return jobRepository.findByTitleContainingIgnoreCaseAndStatusIgnoreCaseOrderByPostedDateDesc(
                title.trim(),
                STATUS_ACTIVE
        );
    }

    public List<Job> searchActiveJobsByLocation(String location) {
        if (isBlank(location)) {
            throw new ValidationException("Job location search term is required");
        }

        return jobRepository.findByLocationContainingIgnoreCaseAndStatusIgnoreCaseOrderByPostedDateDesc(
                location.trim(),
                STATUS_ACTIVE
        );
    }

    public List<Job> filterActiveJobsByCategory(String category) {
        if (isBlank(category)) {
            throw new ValidationException("Job category is required");
        }

        return jobRepository.findByCategoryIgnoreCaseAndStatusIgnoreCaseOrderByPostedDateDesc(
                category.trim(),
                STATUS_ACTIVE
        );
    }

    public List<Job> filterActiveJobsByEmploymentType(String employmentType) {
        if (isBlank(employmentType)) {
            throw new ValidationException("Employment type is required");
        }

        return jobRepository.findByEmploymentTypeIgnoreCaseAndStatusIgnoreCaseOrderByPostedDateDesc(
                employmentType.trim(),
                STATUS_ACTIVE
        );
    }

    public Job closeJob(Long jobId) {
        Job job = getJobById(jobId);
        job.setStatus(STATUS_CLOSED);
        return jobRepository.save(job);
    }

    public void deleteJob(Long jobId) {
        Job job = getJobById(jobId);
        if (applicationRepository.existsByJobJobId(jobId)) {
            throw new JobDeletionBlockedException(
                    "This job cannot be deleted because applications are linked to it"
            );
        }
        jobRepository.delete(job);
    }

    private void validateJob(Job job) {
        if (job == null) {
            throw new ValidationException("Job is required");
        }
        if (isBlank(job.getTitle())) {
            throw new ValidationException("Job title is required");
        }
        if (isBlank(job.getCompanyName())) {
            throw new ValidationException("Company name is required");
        }
        if (isBlank(job.getLocation())) {
            throw new ValidationException("Location is required");
        }
        if (isBlank(job.getCategory())) {
            throw new ValidationException("Category is required");
        }
        if (isBlank(job.getEmploymentType())) {
            throw new ValidationException("Employment type is required");
        }
        if (isBlank(job.getDescription())) {
            throw new ValidationException("Job description is required");
        }
        if (isBlank(job.getRequirements())) {
            throw new ValidationException("Job requirements are required");
        }
        normalizeStatus(job.getStatus());
    }

    private String normalizeStatus(String status) {
        if (isBlank(status)) {
            return STATUS_ACTIVE;
        }

        String normalizedStatus = status.trim().toUpperCase();
        if (!STATUS_ACTIVE.equals(normalizedStatus) && !STATUS_CLOSED.equals(normalizedStatus)) {
            throw new ValidationException("Job status must be ACTIVE or CLOSED");
        }

        return normalizedStatus;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
