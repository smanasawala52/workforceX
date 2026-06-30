package com.workforcex.backend.dto;

import com.workforcex.backend.entity.Role;
import com.workforcex.backend.entity.User;

import java.util.UUID;

public record LoginResponse(
        UUID id,
        String mobileNumber,
        Role role
) {
    public static LoginResponse fromEntity(User user) {
        return new LoginResponse(user.getId(), user.getMobileNumber(), user.getRole());
    }
}
