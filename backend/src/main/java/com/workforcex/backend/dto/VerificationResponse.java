package com.workforcex.backend.dto;

import com.workforcex.backend.entity.Verification;
import com.workforcex.backend.entity.VerificationStatus;
import com.workforcex.backend.entity.VerificationType;

import java.util.UUID;

public record VerificationResponse(
        UUID id,
        UUID userId,
        VerificationType verificationType,
        VerificationStatus status
) {
    public static VerificationResponse fromEntity(Verification verification) {
        return new VerificationResponse(
                verification.getId(),
                verification.getUser().getId(),
                verification.getVerificationType(),
                verification.getStatus()
        );
    }
}
