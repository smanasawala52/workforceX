package com.workforcex.backend.dto;

import com.workforcex.backend.entity.User;
import com.workforcex.backend.entity.Role;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String mobileNumber,
        Role role
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullMobileNumber(),
                user.getRole()
        );
    }
}
