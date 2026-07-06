package com.workforcex.employer.api;

import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("id")
    public String id;

    @SerializedName("message")
    public String message;

    @SerializedName("read")
    public boolean isRead;

    @SerializedName("createdAt")
    public String createdAt;
}
