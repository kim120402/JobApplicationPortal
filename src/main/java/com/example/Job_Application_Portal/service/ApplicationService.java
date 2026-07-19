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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {
    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_REVIEWED = "REVIEWED";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_REJECTED = "REJECTED";

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            UserRepository userRepository,
            JobRepository jobRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }

    public Application submitApplication(Long userId, Long jobId, String resumeFileName, String coverLetter) {
        // validate Id and fileName
        if (userId == null) {
            throw new ValidationException("User id is required");
        }
        if (jobId == null) {
            throw new ValidationException("Job id is required");
        }
        if (isBlank(resumeFileName)) {
            throw new ValidationException("Resume file name is required");
        }

        // check if userId and jobId is in database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // Validate applicant only
        if (!"APPLICANT".equalsIgnoreCase(user.getRole())) {
            throw new InvalidOperationException("Only applicants can submit job applications");
        }
        //Status should be active
        if (!JobService.STATUS_ACTIVE.equalsIgnoreCase(job.getStatus())) {
            throw new InvalidOperationException("Applications can only be submitted for active jobs");
        }
        // check if id exist
        if (applicationRepository.existsByUserUserIdAndJobJobId(userId, jobId)) {
            throw new DuplicateResourceException("User has already applied for this job");
        }

        // Save the data
        Application application = new Application();
        application.setUser(user);
        application.setJob(job);
        application.setResumeFileName(resumeFileName.trim());
        application.setCoverLetter(coverLetter == null ? null : coverLetter.trim());
        application.setApplicationStatus(STATUS_SUBMITTED);
        application.setAppliedDate(LocalDateTime.now());

        // save application data using save(obj)
        return applicationRepository.save(application);
    }

    
    public Application getApplicationById(Long applicationId) {
        if (applicationId == null) {
            throw new ValidationException("Application id is required");
        }

        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    public Application getUserApplication(Long applicationId, Long userId) {
        if (applicationId == null) {
            throw new ValidationException("Application id is required");
        }
        if (userId == null) {
            throw new ValidationException("User id is required");
        }

        return applicationRepository.findByApplicationIdAndUserUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    public List<Application> getApplicationsByUser(Long userId) {
        if (userId == null) {
            throw new ValidationException("User id is required");
        }

        return applicationRepository.findByUserUserIdOrderByAppliedDateDesc(userId);
    }

    public List<Application> getApplicationsByJob(Long jobId) {
        if (jobId == null) {
            throw new ValidationException("Job id is required");
        }

        return applicationRepository.findByJobJobIdOrderByAppliedDateDesc(jobId);
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAllByOrderByAppliedDateDesc();
    }

    public List<Application> getApplicationsByStatus(String status) {
        return applicationRepository.findByApplicationStatusIgnoreCaseOrderByAppliedDateDesc(
                normalizeApplicationStatus(status)
        );
    }

    public Application updateApplicationStatus(Long applicationId, String status) {
        Application application = getApplicationById(applicationId);
        application.setApplicationStatus(normalizeApplicationStatus(status));
        return applicationRepository.save(application);
    }

    public long countApplicationsByStatus(String status) {
        return applicationRepository.countByApplicationStatusIgnoreCase(normalizeApplicationStatus(status));
    }

    private String normalizeApplicationStatus(String status) {
        if (isBlank(status)) {
            throw new ValidationException("Application status is required");
        }

        String normalizedStatus = status.trim().toUpperCase();
        if (!STATUS_SUBMITTED.equals(normalizedStatus)
                && !STATUS_REVIEWED.equals(normalizedStatus)
                && !STATUS_ACCEPTED.equals(normalizedStatus)
                && !STATUS_REJECTED.equals(normalizedStatus)) {
            throw new ValidationException("Application status must be SUBMITTED, REVIEWED, ACCEPTED, or REJECTED");
        }

        return normalizedStatus;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
