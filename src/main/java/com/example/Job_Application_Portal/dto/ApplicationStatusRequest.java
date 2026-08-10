package com.example.Job_Application_Portal.dto;

import jakarta.validation.constraints.NotBlank;

public record ApplicationStatusRequest(
        @NotBlank(message = "Application status is required") String status
) {
}
