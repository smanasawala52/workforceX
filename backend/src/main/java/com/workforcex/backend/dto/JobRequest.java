package com.workforcex.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record JobRequest(
        @NotBlank(message = "Title is required")
        String title,

        String skillsRequired,          // comma-separated

        Integer experienceRequired,

        // Comma-separated cities, e.g. "Mumbai,Pune,Thane"
        String location,

        Double salaryMin,
        Double salaryMax,

        @Min(value = 1, message = "At least 1 position required")
        Integer openPositions,

        String description
) {
}
