package com.workforcex.backend.dto;

import com.workforcex.backend.entity.Role;
import com.workforcex.backend.entity.User;

import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String countryCode,
        String mobileNumber,
        String fullMobileNumber,   // e.g. +919876543210
        Role role
) {
    public static RegisterResponse fromEntity(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getCountryCode(),
                user.getMobileNumber(),
                user.getFullMobileNumber(),
                user.getRole()
        );
    }
}
