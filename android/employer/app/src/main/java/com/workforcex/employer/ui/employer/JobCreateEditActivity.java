package com.workforcex.employer.ui.employer;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.workforcex.employer.api.JobRequest;
import com.workforcex.employer.api.JobResponse;
import com.workforcex.employer.api.RetrofitClient;
import com.workforcex.employer.api.Skill;
import com.workforcex.employer.databinding.ActivityJobCreateEditBinding;
import com.workforcex.employer.utils.TokenManager;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JobCreateEditActivity extends AppCompatActivity {

    private ActivityJobCreateEditBinding binding;
    private TokenManager tokenManager;
    private String jobId;

    private final Set<String> selectedSkills = new LinkedHashSet<>();
    private List<String> allSkills = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJobCreateEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tokenManager = new TokenManager(this);

        jobId = getIntent().getStringExtra("jobId");
        boolean isEditing = jobId != null;
        setTitle(isEditing ? "Edit Job" : "Create Job");

        fetchSkillsAndSetupAutocomplete();

        if (isEditing) {
            binding.etTitle.setText(getIntent().getStringExtra("jobTitle"));
            addSkillsFromCsv(getIntent().getStringExtra("jobSkills"));
            int exp = getIntent().getIntExtra("jobExperience", 0);
            if (exp > 0) binding.etExperience.setText(String.valueOf(exp));
            binding.etLocation.setText(getIntent().getStringExtra("jobLocation"));
            double salMin = getIntent().getDoubleExtra("jobSalaryMin", 0);
            double salMax = getIntent().getDoubleExtra("jobSalaryMax", 0);
            if (salMin > 0) binding.etSalaryMin.setText(String.valueOf((int) salMin));
            if (salMax > 0) binding.etSalaryMax.setText(String.valueOf((int) salMax));
            int positions = getIntent().getIntExtra("jobOpenPositions", 0);
            if (positions > 0) binding.etOpenPositions.setText(String.valueOf(positions));
            binding.etDescription.setText(getIntent().getStringExtra("jobDescription"));
        }

        binding.btnSave.setText(isEditing ? "Update Job" : "Post Job");
        binding.btnSave.setOnClickListener(v -> saveJob());
    }

    private void fetchSkillsAndSetupAutocomplete() {
        RetrofitClient.get().getSkills().enqueue(new Callback<List<Skill>>() {
            @Override
            public void onResponse(Call<List<Skill>> call, Response<List<Skill>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allSkills = response.body().stream().map(s -> s.name).collect(Collectors.toList());
                    setupSkillsAutocomplete();
                }
            }
            @Override
            public void onFailure(Call<List<Skill>> call, Throwable t) {}
        });
    }

    private void setupSkillsAutocomplete() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, allSkills);
        binding.acSkills.setAdapter(adapter);

        binding.acSkills.setOnItemClickListener((parent, view, position, id) -> {
            String skill = (String) parent.getItemAtPosition(position);
            addSkillChip(skill);
            binding.acSkills.setText("");
        });

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
        if (selectedSkills.size() >= 5) {
            Toast.makeText(this, "You can add a maximum of 5 skills", Toast.LENGTH_SHORT).show();
            return;
        }
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

    private void saveJob() {
        String title = binding.etTitle.getText().toString().trim();
        if (title.isEmpty()) { binding.etTitle.setError("Title is required"); return; }

        JobRequest request = new JobRequest();
        request.title = title;
        request.skillsRequired = getSkillsCsv();
        request.location = binding.etLocation.getText().toString().trim();
        request.description = binding.etDescription.getText().toString().trim();

        String expStr = binding.etExperience.getText().toString().trim();
        request.experienceRequired = expStr.isEmpty() ? null : Integer.parseInt(expStr);

        String salMinStr = binding.etSalaryMin.getText().toString().trim();
        request.salaryMin = salMinStr.isEmpty() ? null : Double.parseDouble(salMinStr);

        String salMaxStr = binding.etSalaryMax.getText().toString().trim();
        request.salaryMax = salMaxStr.isEmpty() ? null : Double.parseDouble(salMaxStr);

        String posStr = binding.etOpenPositions.getText().toString().trim();
        request.openPositions = posStr.isEmpty() ? null : Integer.parseInt(posStr);

        setLoading(true);

        Callback<JobResponse> callback = new Callback<JobResponse>() {
            @Override
            public void onResponse(Call<JobResponse> call, Response<JobResponse> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(JobCreateEditActivity.this,
                            jobId != null ? "Job updated!" : "Job posted!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(JobCreateEditActivity.this, "Failed to save job", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<JobResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(JobCreateEditActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        };

        if (jobId != null) {
            RetrofitClient.get().updateJob(tokenManager.getBearerToken(), jobId, request).enqueue(callback);
        } else {
            RetrofitClient.get().createJob(tokenManager.getBearerToken(), request).enqueue(callback);
        }
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSave.setEnabled(!loading);
    }
}
