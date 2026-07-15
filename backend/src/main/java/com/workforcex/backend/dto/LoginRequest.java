package com.workforcex.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        String countryCode,

        @NotBlank(message = "Mobile number is required")
        String mobileNumber,

        @NotBlank(message = "Password is required")
        String password
) {
    // Compact canonical constructor — apply default if null
    public LoginRequest {
        if (countryCode == null || countryCode.isBlank()) {
            countryCode = "+91";
        }
        // Ensure it always starts with +
        if (!countryCode.startsWith("+")) {
            countryCode = "+" + countryCode;
        }
    }
}
