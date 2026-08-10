package com.example.Job_Application_Portal.dto;

import com.example.Job_Application_Portal.model.Job;

import java.time.LocalDate;

public record JobResponse(
        Long jobId,
        String title,
        String companyName,
        String location,
        String category,
        String employmentType,
        String description,
        String requirements,
        String status,
        LocalDate postedDate
) {
    public static JobResponse from(Job job) {
        return new JobResponse(
                job.getJobId(),
                job.getTitle(),
                job.getCompanyName(),
                job.getLocation(),
                job.getCategory(),
                job.getEmploymentType(),
                job.getDescription(),
                job.getRequirements(),
                job.getStatus(),
                job.getPostedDate()
        );
    }
}
