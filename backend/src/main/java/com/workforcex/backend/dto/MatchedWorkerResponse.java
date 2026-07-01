package com.workforcex.backend.dto;

import com.workforcex.backend.entity.WorkerProfile;

import java.util.UUID;

public record MatchedWorkerResponse(
        UUID workerId,
        String name,
        String mobileNumber,
        String skills,
        Integer experience,
        String city,
        Double preferredSalary,
        double score // 0.0 - 100.0
) {
    public static MatchedWorkerResponse fromProfile(WorkerProfile profile, double score) {
        return new MatchedWorkerResponse(
                profile.getId(),
                profile.getName(),
                profile.getUser().getMobileNumber(),
                profile.getSkills(),
                profile.getExperience(),
                profile.getCity(),
                profile.getPreferredSalary(),
                score
        );
    }
}
