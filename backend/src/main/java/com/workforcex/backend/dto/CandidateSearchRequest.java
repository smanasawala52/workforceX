package com.workforcex.backend.dto;

/**
 * Query parameters for candidate search.
 * All fields optional — only supplied filters are applied.
 * Passed as @RequestParam (query string), not request body.
 */
public record CandidateSearchRequest(
        String skills,          // comma-separated, e.g. "driving,security"
        String city,            // single city name
        Integer experienceMin,  // minimum years experience
        Integer experienceMax,  // maximum years experience
        Double salaryMin,       // worker's expected salary lower bound
        Double salaryMax        // worker's expected salary upper bound
) {
}
