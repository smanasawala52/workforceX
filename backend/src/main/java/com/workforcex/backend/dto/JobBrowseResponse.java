package com.workforcex.backend.dto;

import com.workforcex.backend.entity.Job;

import java.util.UUID;

/**
 * What a WORKER sees when browsing jobs.
 * Includes company name so worker knows who posted the job.
 * Excludes internal employer ID details.
 */
public record JobBrowseResponse(
        UUID id,
        String title,
        String companyName,     // from employer profile (if set), else "Unknown Company"
        String employerMobileNumber,     // from employer profile (if set), else "Unknown Company"
        String skillsRequired,
        Integer experienceRequired,
        String location,
        Double salaryMin,
        Double salaryMax,
        Integer openPositions,
        String description
) {
    public static JobBrowseResponse fromEntity(Job job) {
        String companyName=job.getCompanyName() != null ? job.getCompanyName():"Unknown Company";
        return new JobBrowseResponse(
                job.getId(),
                job.getTitle(),
                companyName,
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
