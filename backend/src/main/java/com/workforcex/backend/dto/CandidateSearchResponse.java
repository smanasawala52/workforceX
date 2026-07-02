package com.workforcex.backend.dto;

import com.workforcex.backend.entity.WorkerProfile;

import java.util.UUID;

/**
 * A candidate returned from search — includes score breakdown so employers
 * can see exactly why a candidate ranked where they did.
 */
public record CandidateSearchResponse(
        UUID workerId,
        String name,
        String mobileNumber,
        String skills,
        Integer experience,
        String city,
        String state,
        Double preferredSalary,
        double totalScore,      // 0-100
        double skillScore,      // 0-100
        double experienceScore, // 0-100
        double locationScore,   // 0-100
        double salaryScore      // 0-100
) {
    public static CandidateSearchResponse fromProfile(
            WorkerProfile profile,
            double totalScore,
            double skillScore,
            double experienceScore,
            double locationScore,
            double salaryScore
    ) {
        return new CandidateSearchResponse(
                profile.getId(),
                profile.getName(),
                profile.getUser().getMobileNumber(),
                profile.getSkills(),
                profile.getExperience(),
                profile.getCity(),
                profile.getState(),
                profile.getPreferredSalary(),
                Math.round(totalScore * 10.0) / 10.0,
                Math.round(skillScore * 10.0) / 10.0,
                Math.round(experienceScore * 10.0) / 10.0,
                Math.round(locationScore * 10.0) / 10.0,
                Math.round(salaryScore * 10.0) / 10.0
        );
    }
}
