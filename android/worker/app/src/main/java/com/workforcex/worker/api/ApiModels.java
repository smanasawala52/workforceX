package com.workforcex.worker.api;

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

public class WorkerProfileRequest {
    public String name;
    public String gender;
    public String city;
    public String state;
    public String skills;
    public Integer experience;
    public Double preferredSalary;
}

public class WorkerProfileResponse {
    public String id;
    public String mobileNumber;
    public String name;
    public String gender;
    public String city;
    public String state;
    public String skills;
    public Integer experience;
    public Double preferredSalary;
}
