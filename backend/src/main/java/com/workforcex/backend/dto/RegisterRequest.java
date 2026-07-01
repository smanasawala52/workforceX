package com.workforcex.backend.dto;

import com.workforcex.backend.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(

        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits")
        String mobileNumber,

        @NotNull(message = "Role is required")
        Role role,

        // Country code defaults to India (+91) if not provided
        String countryCode

) {
    // Compact canonical constructor — apply default if null
    public RegisterRequest {
        if (countryCode == null || countryCode.isBlank()) {
            countryCode = "+91";
        }
        // Ensure it always starts with +
        if (!countryCode.startsWith("+")) {
            countryCode = "+" + countryCode;
        }
    }
}
