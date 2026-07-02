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
