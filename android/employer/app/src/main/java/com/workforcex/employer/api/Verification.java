package com.workforcex.employer.api;

import com.google.gson.annotations.SerializedName;

public class Verification {
    @SerializedName("id")
    public String id;

    @SerializedName("user")
    public User user;

    @SerializedName("verificationType")
    public String verificationType;

    @SerializedName("status")
    public String status;

    public static class User {
        @SerializedName("mobileNumber")
        public String mobileNumber;
    }
}
