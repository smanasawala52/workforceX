package com.workforcex.backend.dto;

import com.workforcex.backend.entity.ApplicationStatus;
import com.workforcex.backend.entity.WorkerProfile;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record MatchedWorkerResponse(
        UUID workerId,
        String name,
        String mobileNumber,
        String skills,
        Integer experience,
        String city,
        Double preferredSalary,
        double score, // 0.0 - 100.0
        ApplicationStatus applicationStatus
) {
    public static MatchedWorkerResponse fromProfile(WorkerProfile profile, double score, ApplicationStatus status) {
        return new MatchedWorkerResponse(
                profile.getUserId(),
                profile.getName(),
                profile.getUserMobileNumber(),
                getMergedSkills(profile.getSkill1(),
                        profile.getSkill2(),
                        profile.getSkill3(),
                        profile.getSkill4(),
                        profile.getSkill5()),
                profile.getExperience(),
                profile.getCity(),
                profile.getPreferredSalary(),
                score,
                status
        );
    }
    private static String getMergedSkills(String skill1, String skill2, String skill3, String skill4, String skill5) {
        return Stream.of(skill1, skill2, skill3, skill4, skill5)
                .filter(s -> s != null && !s.isEmpty()) // Ignore empty or null skills
                .collect(Collectors.joining(","));
    }
}
