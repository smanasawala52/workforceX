package com.workforcex.backend.dto;

import com.workforcex.backend.entity.Job;

import java.util.UUID;

public record JobResponse(
        UUID id,
        UUID employerId,
        String title,
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
                job.getEmployer().getId(),
                job.getTitle(),
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
