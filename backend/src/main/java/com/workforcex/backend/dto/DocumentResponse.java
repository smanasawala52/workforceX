package com.workforcex.backend.dto;

import com.workforcex.backend.entity.DocumentType;

import java.time.Instant;
import java.util.UUID;

/**
 * What worker + employer apps actually need to show an uploaded document:
 * what it is, its original filename, when it was uploaded, and a URL that's
 * valid right now to view/download it.
 */
public record DocumentResponse(
        UUID id,
        DocumentType documentType,
        String fileName,
        String fileUrl,
        Instant createdAt
) {
}
