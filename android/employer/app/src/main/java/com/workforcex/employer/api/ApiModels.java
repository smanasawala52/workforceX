package com.workforcex.employer.api;

// ── Auth ─────────────────────────────────────────────────────────────────────

class RegisterRequest {
    public String mobileNumber;
    public String role;
    public RegisterRequest(String mobileNumber, String role) {
        this.mobileNumber = mobileNumber;
        this.role = role;
    }
}

class LoginRequest {
    public String mobileNumber;
    public String password;
    public LoginRequest(String mobileNumber, String password) {
        this.mobileNumber = mobileNumber;
        this.password = password;
    }
}

class RegisterResponse {
    public String id;
    public String mobileNumber;
    public String role;
}

class LoginResponse {
    public String id;
    public String mobileNumber;
    public String role;
    public String token;
}

// ── Employer Profile ──────────────────────────────────────────────────────────

class EmployerProfileRequest {
    public String companyName;
    public String contactPerson;
    public String email;
    public String address;
}

class EmployerProfileResponse {
    public String id;
    public String mobileNumber;
    public String companyName;
    public String contactPerson;
    public String email;
    public String address;
}

// ── Job ───────────────────────────────────────────────────────────────────────

class JobRequest {
    public String title;
    public String skillsRequired;
    public Integer experienceRequired;
    public String location;         // comma-separated cities
    public Double salaryMin;
    public Double salaryMax;
    public Integer openPositions;
    public String description;
}

class JobResponse {
    public String id;
    public String employerId;
    public String title;
    public String skillsRequired;
    public Integer experienceRequired;
    public String location;         // comma-separated cities
    public Double salaryMin;
    public Double salaryMax;
    public Integer openPositions;
    public String description;
}

// ── Matching ──────────────────────────────────────────────────────────────────

class MatchedWorker {
    public String workerId;
    public String name;
    public String mobileNumber;
    public String skills;
    public Integer experience;
    public String city;
    public Double preferredSalary;
    public Double score;
}
