package com.workforcex.employer.api;

// This class must exactly match the fields from the backend's JobResponse record.
public class JobResponse {
    public String id;
    public String employerId;
    public String title;
    public String skillsRequired;
    public Integer experienceRequired;
    public String location;
    public Double salaryMin;
    public Double salaryMax;
    public Integer openPositions;
    public String description;
}
