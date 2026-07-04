package com.workforcex.backend.dto;

import java.util.List;

/**
 * What the API returns after parsing a resume PDF.
 * Includes extracted data so the Android app can show a preview.
 */
public record ResumeParseResponse(
        String fileName,
        String extractedSkills,         // comma-separated skills found in resume
        List<String> detectedSkillList, // same as above but as a list for the UI
        Integer detectedExperience,     // years detected (null if not found)
        String rawTextPreview,          // first 500 chars of extracted text
        String message
) {
}
