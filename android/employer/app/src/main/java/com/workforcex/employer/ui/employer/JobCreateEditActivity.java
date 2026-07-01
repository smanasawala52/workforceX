package com.workforcex.employer.ui.employer;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.workforcex.employer.api.JobRequest;
import com.workforcex.employer.api.JobResponse;
import com.workforcex.employer.api.RetrofitClient;
import com.workforcex.employer.databinding.ActivityJobCreateEditBinding;
import com.workforcex.employer.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JobCreateEditActivity extends AppCompatActivity {

    private ActivityJobCreateEditBinding binding;
    private TokenManager tokenManager;
    private String jobId; // null = create mode, non-null = edit mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJobCreateEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tokenManager = new TokenManager(this);

        jobId = getIntent().getStringExtra("jobId");
        boolean isEditing = jobId != null;

        setTitle(isEditing ? "Edit Job" : "Create Job");

        if (isEditing) {
            binding.etTitle.setText(getIntent().getStringExtra("jobTitle"));
            binding.etSkills.setText(getIntent().getStringExtra("jobSkills"));
            int exp = getIntent().getIntExtra("jobExperience", 0);
            if (exp > 0) binding.etExperience.setText(String.valueOf(exp));
            binding.etLocation.setText(getIntent().getStringExtra("jobLocation"));
            double salary = getIntent().getDoubleExtra("jobSalary", 0);
            if (salary > 0) binding.etSalary.setText(String.valueOf((int) salary));
            binding.etDescription.setText(getIntent().getStringExtra("jobDescription"));
        }

        binding.btnSave.setText(isEditing ? "Update Job" : "Post Job");
        binding.btnSave.setOnClickListener(v -> saveJob());
    }

    private void saveJob() {
        String title = binding.etTitle.getText().toString().trim();
        if (title.isEmpty()) { binding.etTitle.setError("Title is required"); return; }

        JobRequest request = new JobRequest();
        request.title = title;
        request.skillsRequired = binding.etSkills.getText().toString().trim();
        request.location = binding.etLocation.getText().toString().trim();
        request.description = binding.etDescription.getText().toString().trim();

        String expStr = binding.etExperience.getText().toString().trim();
        request.experienceRequired = expStr.isEmpty() ? null : Integer.parseInt(expStr);
        String salStr = binding.etSalary.getText().toString().trim();
        request.salary = salStr.isEmpty() ? null : Double.parseDouble(salStr);

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
