package com.workforcex.backend.dto;

import java.time.LocalDate;

/**
 * What a worker can submit to create/update their profile.
 * All fields optional, per registration philosophy.
 */
public record WorkerProfileRequest(
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
}
