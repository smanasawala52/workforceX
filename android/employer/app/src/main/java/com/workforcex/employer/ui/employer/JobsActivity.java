package com.workforcex.employer.ui.employer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.workforcex.employer.api.JobResponse;
import com.workforcex.employer.api.RetrofitClient;
import com.workforcex.employer.databinding.ActivityJobsBinding;
import com.workforcex.employer.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class JobsActivity extends AppCompatActivity {

    private ActivityJobsBinding binding;
    private TokenManager tokenManager;
    private JobAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJobsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tokenManager = new TokenManager(this);

        adapter = new JobAdapter(new ArrayList<>(), this::onFindCandidates, this::onEditJob);
        binding.rvJobs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvJobs.setAdapter(adapter);

        binding.btnCreateJob.setOnClickListener(v -> {
            Intent intent = new Intent(this, JobCreateEditActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadJobs();
    }

    private void loadJobs() {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.get().getMyJobs(tokenManager.getBearerToken())
                .enqueue(new Callback<List<JobResponse>>() {
                    @Override
                    public void onResponse(Call<List<JobResponse>> call, Response<List<JobResponse>> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.updateJobs(response.body());
                            binding.tvEmpty.setVisibility(response.body().isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }
                    @Override
                    public void onFailure(Call<List<JobResponse>> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(JobsActivity.this, "Failed to load jobs", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onFindCandidates(JobResponse job) {
        Intent intent = new Intent(this, MatchingResultsActivity.class);
        intent.putExtra("jobId", job.id);
        intent.putExtra("jobTitle", job.title);
        startActivity(intent);
    }

    private void onEditJob(JobResponse job) {
        Intent intent = new Intent(this, JobCreateEditActivity.class);
        intent.putExtra("jobId", job.id);
        intent.putExtra("jobTitle", job.title);
        intent.putExtra("jobSkills", job.skillsRequired);
        intent.putExtra("jobExperience", job.experienceRequired != null ? job.experienceRequired : 0);
        intent.putExtra("jobLocation", job.location);
        intent.putExtra("jobSalary", job.salary != null ? job.salary : 0.0);
        intent.putExtra("jobDescription", job.description);
        startActivity(intent);
    }
}
