package com.workforcex.backend.dto;

import com.workforcex.backend.entity.Job;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                getMergedSkills(job.getSkillsRequired1(),
                        job.getSkillsRequired2(),
                        job.getSkillsRequired3(),
                        job.getSkillsRequired4(),
                        job.getSkillsRequired5()),
                job.getExperienceRequired(),
                job.getLocation(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getOpenPositions(),
                job.getDescription()
        );
    }
    private static String getMergedSkills(String skill1, String skill2, String skill3, String skill4, String skill5) {
        return Stream.of(skill1, skill2, skill3, skill4, skill5)
                .filter(s -> s != null && !s.isEmpty()) // Ignore empty or null skills
                .collect(Collectors.joining(","));
    }
}