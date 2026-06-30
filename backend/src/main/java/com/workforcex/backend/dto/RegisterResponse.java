package com.workforcex.backend.dto;

import com.workforcex.backend.entity.Role;
import com.workforcex.backend.entity.User;

import java.util.UUID;

/**
 * What we send back after registration.
 * Never includes the password hash.
 */
public record RegisterResponse(
        UUID id,
        String mobileNumber,
        Role role
) {
    public static RegisterResponse fromEntity(User user) {
        return new RegisterResponse(user.getId(), user.getMobileNumber(), user.getRole());
    }
}
