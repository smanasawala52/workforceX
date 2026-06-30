package com.workforcex.backend.common.exception;

import java.time.Instant;

/**
 * Consistent shape for every error response the API returns.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String message
) {
    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(Instant.now(), status, message);
    }
}
