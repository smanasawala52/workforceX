package com.workforcex.backend.dto;

import com.workforcex.backend.entity.WorkerProfile;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Double preferredSalary,
        String description
) {
    public static WorkerProfileResponse fromEntity(WorkerProfile profile) {
        return new WorkerProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getUserMobileNumber(),
                profile.getName(),
                profile.getGender(),
                profile.getDateOfBirth(),
                profile.getEmail(),
                profile.getAddress(),
                profile.getCity(),
                profile.getState(),
                getMergedSkills(profile.getSkill1(),
                        profile.getSkill2(),
                        profile.getSkill3(),
                        profile.getSkill4(),
                        profile.getSkill5()),
                profile.getExperience(),
                profile.getPreferredSalary(),
                profile.getDescription()
        );
    }
    public static String getMergedSkills(String skill1, String skill2, String skill3, String skill4, String skill5) {
        return Stream.of(skill1, skill2, skill3, skill4, skill5)
                .filter(s -> s != null && !s.isEmpty()) // Ignore empty or null skills
                .collect(Collectors.joining(","));
    }
}
