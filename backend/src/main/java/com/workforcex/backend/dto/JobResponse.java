package com.workforcex.backend.dto;

import com.workforcex.backend.entity.Job;

import java.util.UUID;

public record JobResponse(
        UUID id,
        UUID employerId,
        String title,
        String companyName,
        String employerMobileNumber,
        String skillsRequired,
        Integer experienceRequired,
        String location,            // comma-separated cities
        Double salaryMin,
        Double salaryMax,
        Integer openPositions,
        String description
) {
    public static JobResponse fromEntity(Job job) {
        return new JobResponse(
                job.getId(),
                job.getEmployerId(),
                job.getTitle(),
                job.getCompanyName(),
                job.getEmployerMobileNumber(),
                job.getSkillsRequired(),
                job.getExperienceRequired(),
                job.getLocation(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getOpenPositions(),
                job.getDescription()
        );
    }
}
