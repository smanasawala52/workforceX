package com.workforcex.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record JobRequest(
        @NotBlank(message = "Title is required")
        String title,

        String skillsRequired,
        Integer experienceRequired,
        String location,
        Double salary,
        String description
) {
}
