package com.workforcex.worker.api;
public class LoginRequest {
    public String mobileNumber;
    public String password;
    public LoginRequest(String mobileNumber, String password) {
        this.mobileNumber = mobileNumber;
        this.password = password;
    }
}
