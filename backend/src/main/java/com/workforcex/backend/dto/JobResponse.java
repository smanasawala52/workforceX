package com.workforcex.backend.dto;

import com.workforcex.backend.entity.Job;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
