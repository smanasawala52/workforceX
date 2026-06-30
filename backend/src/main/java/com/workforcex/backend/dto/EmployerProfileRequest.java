package com.workforcex.backend.dto;

public record EmployerProfileRequest(
        String companyName,
        String contactPerson,
        String email,
        String address
) {
}
