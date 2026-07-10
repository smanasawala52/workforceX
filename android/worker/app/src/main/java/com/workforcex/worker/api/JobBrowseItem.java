package com.workforcex.worker.api;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class JobBrowseItem implements Serializable {
    @SerializedName("id")
    public String id;
    @SerializedName("title")
    public String title;
    @SerializedName("companyName")
    public String companyName;
    @SerializedName("skillsRequired")
    public String skillsRequired;
    @SerializedName("experienceRequired")
    public Integer experienceRequired;
    @SerializedName("location")
    public String location;
    @SerializedName("salaryMin")
    public Double salaryMin;
    @SerializedName("salaryMax")
    public Double salaryMax;
    @SerializedName("openPositions")
    public Integer openPositions;
    @SerializedName("description")
    public String description;
    public Boolean applied = false; // local UI state — not from API
}
