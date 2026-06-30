package com.workforcex.backend.dto;

import com.workforcex.backend.entity.EmployerProfile;

import java.util.UUID;

public record EmployerProfileResponse(
        UUID id,
        UUID userId,
        String mobileNumber,
        String companyName,
        String contactPerson,
        String email,
        String address
) {
    public static EmployerProfileResponse fromEntity(EmployerProfile profile) {
        return new EmployerProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getMobileNumber(),
                profile.getCompanyName(),
                profile.getContactPerson(),
                profile.getEmail(),
                profile.getAddress()
        );
    }
}
