package com.workforcex.backend.dto;

import com.workforcex.backend.entity.WorkerProfile;

import java.time.LocalDate;
import java.util.UUID;

public record WorkerProfileResponse(
        UUID id,
        UUID userId,
        String mobileNumber,
        String name,
        String gender,
        LocalDate dateOfBirth,
        String email,
        String address,
        String city,
        String state,
        String skills,
        Integer experience,
        Double preferredSalary
) {
    public static WorkerProfileResponse fromEntity(WorkerProfile profile) {
        return new WorkerProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getMobileNumber(),
                profile.getName(),
                profile.getGender(),
                profile.getDateOfBirth(),
                profile.getEmail(),
                profile.getAddress(),
                profile.getCity(),
                profile.getState(),
                profile.getSkills(),
                profile.getExperience(),
                profile.getPreferredSalary()
        );
    }
}
