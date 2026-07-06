package com.workforcex.worker.ui.worker;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.workforcex.worker.api.JobApplication;
import com.workforcex.worker.api.JobBrowseItem;
import com.workforcex.worker.api.RetrofitClient;
import com.workforcex.worker.databinding.ActivityJobDetailsBinding;
import com.workforcex.worker.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JobDetailsActivity extends AppCompatActivity {

    private ActivityJobDetailsBinding binding;
    private TokenManager tokenManager;
    private JobBrowseItem job;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJobDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tokenManager = new TokenManager(this);

        job = (JobBrowseItem) getIntent().getSerializableExtra("job");
        boolean isApplied = getIntent().getBooleanExtra("isApplied", false);

        setTitle("Job Details");
        populateJobDetails();

        binding.btnApply.setEnabled(!isApplied);
        binding.btnApply.setText(isApplied ? "Already Applied" : "Apply Now");
        binding.btnApply.setOnClickListener(v -> {
            if (!isApplied) {
                applyToJob();
            }
        });
    }

    private void populateJobDetails() {
        binding.tvJobTitle.setText(job.title);
        binding.tvCompanyName.setText(job.companyName);
        binding.tvLocation.setText("📍 " + job.location);
        binding.tvSkills.setText("Skills: " + job.skillsRequired);
        binding.tvExperience.setText("Experience: " + job.experienceRequired + " years");
        binding.tvDescription.setText(job.description);

        if (job.salaryMin != null && job.salaryMax != null) {
            binding.tvSalary.setText("₹" + job.salaryMin.intValue() + " – ₹" + job.salaryMax.intValue() + "/month");
        } else if (job.salaryMin != null) {
            binding.tvSalary.setText("₹" + job.salaryMin.intValue() + "+/month");
        } else {
            binding.tvSalary.setText("Salary not disclosed");
        }
    }

    private void applyToJob() {
        binding.btnApply.setEnabled(false);
        RetrofitClient.get().applyToJob(tokenManager.getBearerToken(), job.id)
                .enqueue(new Callback<JobApplication>() {
                    @Override
                    public void onResponse(Call<JobApplication> call, Response<JobApplication> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(JobDetailsActivity.this, "Applied successfully!", Toast.LENGTH_SHORT).show();
                            binding.btnApply.setText("Applied");
                        } else {
                            Toast.makeText(JobDetailsActivity.this, "Already applied or error.", Toast.LENGTH_SHORT).show();
                            binding.btnApply.setEnabled(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<JobApplication> call, Throwable t) {
                        Toast.makeText(JobDetailsActivity.this, "Network error.", Toast.LENGTH_SHORT).show();
                        binding.btnApply.setEnabled(true);
                    }
                });
    }
}
