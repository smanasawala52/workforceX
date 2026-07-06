package com.workforcex.worker.api;

import java.io.Serializable;

public class JobBrowseItem implements Serializable {
    public String id;
    public String title;
    public String companyName;
    public String skillsRequired;
    public Integer experienceRequired;
    public String location;
    public Double salaryMin;
    public Double salaryMax;
    public Integer openPositions;
    public String description;
    public Boolean applied = false; // local UI state — not from API
}
