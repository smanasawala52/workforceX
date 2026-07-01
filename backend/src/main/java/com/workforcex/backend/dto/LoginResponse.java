package com.workforcex.backend.dto;

import com.workforcex.backend.entity.Role;
import com.workforcex.backend.entity.User;

import java.util.UUID;

public record LoginResponse(
        UUID id,
        String countryCode,
        String mobileNumber,
        String fullMobileNumber,   // e.g. +919876543210
        Role role,
        String token
) {
    public static LoginResponse fromEntity(User user, String token) {
        return new LoginResponse(
                user.getId(),
                user.getCountryCode(),
                user.getMobileNumber(),
                user.getFullMobileNumber(),
                user.getRole(),
                token
        );
    }
}
