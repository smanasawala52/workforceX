package com.workforcex.employer.api;

public class RegisterRequest {
    public String mobileNumber;
    public String role;
    public String countryCode;
    public RegisterRequest(String mobileNumber, String role, String countryCode) {
        this.mobileNumber = mobileNumber;
        this.role = role;
        this.countryCode = countryCode;
    }
}

public class LoginRequest {
    public String mobileNumber;
    public String password;
    public LoginRequest(String mobileNumber, String password) {
        this.mobileNumber = mobileNumber;
        this.password = password;
    }
}

public class RegisterResponse {
    public String id;
    public String countryCode;
    public String mobileNumber;
    public String fullMobileNumber;
    public String role;
}

public class LoginResponse {
    public String id;
    public String countryCode;
    public String mobileNumber;
    public String fullMobileNumber;
    public String role;
    public String token;
}

public class EmployerProfileRequest {
    public String companyName;
    public String contactPerson;
    public String email;
    public String address;
}

public class EmployerProfileResponse {
    public String id;
    public String mobileNumber;
    public String companyName;
    public String contactPerson;
    public String email;
    public String address;
}

public class JobRequest {
    public String title;
    public String skillsRequired;
    public Integer experienceRequired;
    public String location;
    public Double salaryMin;
    public Double salaryMax;
    public Integer openPositions;
    public String description;
}

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

public class MatchedWorker {
    public String workerId;
    public String name;
    public String mobileNumber;
    public String skills;
    public Integer experience;
    public String city;
    public Double preferredSalary;
    public Double score;
}
