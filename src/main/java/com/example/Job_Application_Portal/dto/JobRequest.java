package com.example.Job_Application_Portal.dto;

import jakarta.validation.constraints.NotBlank;

public record JobRequest(
        @NotBlank(message = "Job title is required") String title,
        @NotBlank(message = "Company name is required") String companyName,
        @NotBlank(message = "Location is required") String location,
        @NotBlank(message = "Category is required") String category,
        @NotBlank(message = "Employment type is required") String employmentType,
        @NotBlank(message = "Job description is required") String description,
        @NotBlank(message = "Job requirements are required") String requirements,
        String status
) {
}
