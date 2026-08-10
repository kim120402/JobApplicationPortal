package com.example.Job_Application_Portal.dto;

public record DashboardStatisticsResponse(
        long totalJobs,
        long activeJobs,
        long closedJobs,
        long totalApplicants,
        long totalApplications,
        long underReviewApplications,
        long interviewApplications,
        long hiredApplications
) {
}
