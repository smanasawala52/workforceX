package com.workforcex.worker.ui.worker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.workforcex.worker.api.JobBrowseItem;
import com.workforcex.worker.api.RetrofitClient;
import com.workforcex.worker.api.WorkerProfileRequest;
import com.workforcex.worker.api.WorkerProfileResponse;
import com.workforcex.worker.databinding.ActivityWorkerProfileBinding;
import com.workforcex.worker.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class WorkerProfileActivity extends AppCompatActivity {

    private ActivityWorkerProfileBinding binding;
    private TokenManager tokenManager;

    // All supported skills for autocomplete
    private static final String[] ALL_SKILLS = {
        "security", "patrolling", "surveillance", "cctv", "access-control",
        "driving", "car", "truck", "bus", "bike", "hcv", "lmv", "delivery",
        "housekeeping", "cleaning", "mopping", "sweeping", "laundry",
        "cooking", "kitchen", "chef", "south-indian", "north-indian",
        "electrical", "wiring", "electrician", "plumbing", "plumber",
        "carpentry", "woodwork", "furniture", "painting", "masonry", "construction",
        "welding", "fabrication", "ac-repair", "hvac", "refrigeration",
        "lift", "elevator", "generator", "facility-management", "maintenance",
        "warehouse", "logistics", "inventory", "data-entry", "computer",
        "typing", "ms-office", "reception", "communication", "english",
        "patient-care", "hospital", "nursing", "gardening", "forklift",
        "supervision", "leadership", "reporting", "watchman", "cleaner"
    };

    // Currently selected skills (ordered set to avoid duplicates)
    private final Set<String> selectedSkills = new LinkedHashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorkerProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("My Profile");
        tokenManager = new TokenManager(this);

        setupSkillsAutocomplete();
        loadExistingProfile();
        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void setupSkillsAutocomplete() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, ALL_SKILLS);
        binding.acSkills.setAdapter(adapter);

        // When user selects a suggestion from dropdown
        binding.acSkills.setOnItemClickListener((parent, view, position, id) -> {
            String skill = (String) parent.getItemAtPosition(position);
            addSkillChip(skill);
            binding.acSkills.setText("");
        });

        // When user presses Done on keyboard — add whatever they typed
        binding.acSkills.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String typed = binding.acSkills.getText().toString().trim().toLowerCase();
                if (!typed.isEmpty()) {
                    addSkillChip(typed);
                    binding.acSkills.setText("");
                }
                return true;
            }
            return false;
        });
    }

    private void addSkillChip(String skill) {
        String clean = skill.trim().toLowerCase();
        if (clean.isEmpty() || selectedSkills.contains(clean)) return;

        selectedSkills.add(clean);

        Chip chip = new Chip(this);
        chip.setText(clean);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            selectedSkills.remove(clean);
            binding.chipGroupSkills.removeView(chip);
        });
        binding.chipGroupSkills.addView(chip);
    }

    private void addSkillsFromCsv(String csv) {
        if (csv == null || csv.isBlank()) return;
        for (String skill : csv.split(",")) {
            addSkillChip(skill.trim());
        }
    }

    private String getSkillsCsv() {
        return String.join(",", selectedSkills);
    }

    private void loadExistingProfile() {
        RetrofitClient.get().getWorkerProfile(tokenManager.getBearerToken())
                .enqueue(new Callback<WorkerProfileResponse>() {
                    @Override
                    public void onResponse(Call<WorkerProfileResponse> call, Response<WorkerProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            WorkerProfileResponse p = response.body();
                            if (p.name != null) binding.etName.setText(p.name);
                            if (p.city != null) binding.etCity.setText(p.city);
                            if (p.state != null) binding.etState.setText(p.state);
                            if (p.skills != null) addSkillsFromCsv(p.skills);
                            if (p.experience != null) binding.etExperience.setText(String.valueOf(p.experience));
                            if (p.preferredSalary != null) binding.etSalary.setText(String.valueOf(p.preferredSalary.intValue()));
                        }
                    }
                    @Override
                    public void onFailure(Call<WorkerProfileResponse> call, Throwable t) {}
                });
    }

    private void saveProfile() {
        WorkerProfileRequest request = new WorkerProfileRequest();
        request.name = binding.etName.getText().toString().trim();
        request.city = binding.etCity.getText().toString().trim();
        request.state = binding.etState.getText().toString().trim();
        request.skills = getSkillsCsv();

        String expStr = binding.etExperience.getText().toString().trim();
        request.experience = expStr.isEmpty() ? null : Integer.parseInt(expStr);

        String salStr = binding.etSalary.getText().toString().trim();
        request.preferredSalary = salStr.isEmpty() ? null : Double.parseDouble(salStr);

        setLoading(true);

        RetrofitClient.get().saveWorkerProfile(tokenManager.getBearerToken(), request)
                .enqueue(new Callback<WorkerProfileResponse>() {
                    @Override
                    public void onResponse(Call<WorkerProfileResponse> call, Response<WorkerProfileResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            // After saving, show matched jobs based on their skills
                            showMatchedJobs(request.skills, request.city);
                        } else {
                            Toast.makeText(WorkerProfileActivity.this, "Failed to save profile", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<WorkerProfileResponse> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(WorkerProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showMatchedJobs(String skills, String city) {
        // Navigate to BrowseJobsActivity with skills filter pre-applied
        Intent intent = new Intent(this, BrowseJobsActivity.class);
        intent.putExtra("filterSkills", skills);
        intent.putExtra("filterCity", city);
        intent.putExtra("showMatchedFirst", true);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSave.setEnabled(!loading);
    }
}
