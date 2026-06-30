package com.workforcex.backend.dto;

import com.workforcex.backend.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * What a client is allowed to send when registering.
 * Only mobile number + role — nothing else, per registration philosophy.
 */
public record RegisterRequest(

        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits")
        String mobileNumber,

        @NotNull(message = "Role is required")
        Role role

) {
}
