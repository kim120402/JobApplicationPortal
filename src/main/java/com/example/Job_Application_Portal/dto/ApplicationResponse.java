package com.example.Job_Application_Portal.dto;

import com.example.Job_Application_Portal.model.Application;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Long applicationId,
        Long applicantId,
        String applicantFullName,
        String applicantEmail,
        Long jobId,
        String jobTitle,
        String companyName,
        String jobLocation,
        String employmentType,
        String resumeFileName,
        String coverLetter,
        String applicationStatus,
        LocalDateTime appliedDate
) {
    public static ApplicationResponse from(Application application) {
        return new ApplicationResponse(
                application.getApplicationId(),
                application.getUser().getUserId(),
                application.getUser().getFullName(),
                application.getUser().getEmail(),
                application.getJob().getJobId(),
                application.getJob().getTitle(),
                application.getJob().getCompanyName(),
                application.getJob().getLocation(),
                application.getJob().getEmploymentType(),
                application.getResumeFileName(),
                application.getCoverLetter(),
                application.getApplicationStatus(),
                application.getAppliedDate()
        );
    }
}
