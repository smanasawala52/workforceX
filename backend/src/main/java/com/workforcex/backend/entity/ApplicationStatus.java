package com.workforcex.backend.entity;

public enum ApplicationStatus {
    PENDING,      // just applied, employer hasn't acted
    SHORTLISTED,  // employer is interested
    REJECTED,     // employer passed
    INTERVIEW,    // employer wants to interview
    OFFERED,      // employer has made an offer
    HIRED         // candidate has been hired
}
