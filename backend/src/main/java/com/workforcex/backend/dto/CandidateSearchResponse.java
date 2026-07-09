package com.workforcex.backend.dto;

import com.workforcex.backend.entity.WorkerProfile;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                profile.getUserMobileNumber(),
                getMergedSkills(profile.getSkill1(),
                        profile.getSkill2(),
                        profile.getSkill3(),
                        profile.getSkill4(),
                        profile.getSkill5()),
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
    private static String getMergedSkills(String skill1, String skill2, String skill3, String skill4, String skill5) {
        return Stream.of(skill1, skill2, skill3, skill4, skill5)
                .filter(s -> s != null && !s.isEmpty()) // Ignore empty or null skills
                .collect(Collectors.joining(","));
    }
}
