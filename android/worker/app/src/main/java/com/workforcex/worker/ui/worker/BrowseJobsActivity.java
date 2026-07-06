package com.workforcex.worker.ui.worker;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.workforcex.worker.api.JobApplication;
import com.workforcex.worker.api.JobBrowseItem;
import com.workforcex.worker.api.RetrofitClient;
import com.workforcex.worker.databinding.ActivityBrowseJobsBinding;
import com.workforcex.worker.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BrowseJobsActivity extends AppCompatActivity {

    private ActivityBrowseJobsBinding binding;
    private TokenManager tokenManager;
    private BrowseJobsAdapter adapter;
    private List<JobBrowseItem> allJobs = new ArrayList<>();
    private String filterSkills;
    private String filterCity;
    private boolean showMatchedFirst;
    private boolean currentlyShowingMatched = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrowseJobsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(this);
        adapter = new BrowseJobsAdapter(new ArrayList<>(), this::applyToJob);
        binding.rvJobs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvJobs.setAdapter(adapter);

        filterSkills     = getIntent().getStringExtra("filterSkills");
        filterCity       = getIntent().getStringExtra("filterCity");
        showMatchedFirst = getIntent().getBooleanExtra("showMatchedFirst", false);

        if (showMatchedFirst && filterSkills != null && !filterSkills.isEmpty()) {
            setTitle("Jobs Matching Your Skills");
            binding.btnToggleJobs.setVisibility(View.VISIBLE);
            binding.btnToggleJobs.setText("Show All Jobs");
            binding.tvFilterInfo.setVisibility(View.VISIBLE);
            binding.tvFilterInfo.setText("Matching: " + filterSkills);
        } else {
            setTitle("Available Jobs");
            binding.btnToggleJobs.setVisibility(View.GONE);
            binding.tvFilterInfo.setVisibility(View.GONE);
            currentlyShowingMatched = false;
        }

        binding.btnToggleJobs.setOnClickListener(v -> toggleJobsView());
        loadJobs();
    }

    private void loadJobs() {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.get().browseJobs(tokenManager.getBearerToken())
                .enqueue(new Callback<List<JobBrowseItem>>() {
                    @Override
                    public void onResponse(Call<List<JobBrowseItem>> call,
                                           Response<List<JobBrowseItem>> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            allJobs = response.body();
                            displayJobs(currentlyShowingMatched && showMatchedFirst);
                        }
                    }
                    @Override
                    public void onFailure(Call<List<JobBrowseItem>> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(BrowseJobsActivity.this,
                                "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void toggleJobsView() {
        currentlyShowingMatched = !currentlyShowingMatched;
        displayJobs(currentlyShowingMatched);
        binding.btnToggleJobs.setText(currentlyShowingMatched ? "Show All Jobs" : "Show Matching Jobs");
        binding.tvFilterInfo.setVisibility(currentlyShowingMatched ? View.VISIBLE : View.GONE);
        setTitle(currentlyShowingMatched ? "Jobs Matching Your Skills" :
                "All Jobs (" + allJobs.size() + ")");
    }

    private void displayJobs(boolean matchedOnly) {
        List<JobBrowseItem> toShow = matchedOnly && filterSkills != null && !filterSkills.isEmpty()
                ? filterBySkills(allJobs, filterSkills, filterCity)
                : allJobs;
        adapter.update(toShow);
        binding.tvEmpty.setVisibility(toShow.isEmpty() ? View.VISIBLE : View.GONE);
        binding.tvResultCount.setVisibility(View.VISIBLE);
        binding.tvResultCount.setText(toShow.size() +
                (matchedOnly ? " job(s) match your skills" : " total jobs available"));
    }

    private void applyToJob(JobBrowseItem job, int position) {
        RetrofitClient.get().applyToJob(tokenManager.getBearerToken(), job.id)
                .enqueue(new Callback<JobApplication>() {
                    @Override
                    public void onResponse(Call<JobApplication> call,
                                           Response<JobApplication> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(BrowseJobsActivity.this,
                                    "Applied to " + job.title + "! ✓", Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 400) {
                            Toast.makeText(BrowseJobsActivity.this,
                                    "Already applied to this job", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BrowseJobsActivity.this,
                                    "Failed to apply", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<JobApplication> call, Throwable t) {
                        Toast.makeText(BrowseJobsActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private List<JobBrowseItem> filterBySkills(List<JobBrowseItem> jobs, String skills, String city) {
        Set<String> workerSkills = new HashSet<>(Arrays.asList(skills.toLowerCase().split(",")));
        return jobs.stream().filter(job -> {
            if (job.skillsRequired == null) return false;
            Set<String> jobSkills = new HashSet<>(Arrays.asList(job.skillsRequired.toLowerCase().split(",")));
            boolean match = jobSkills.stream().anyMatch(workerSkills::contains);
            if (!match) return false;
            if (city != null && !city.isEmpty() && job.location != null)
                return job.location.toLowerCase().contains(city.toLowerCase());
            return true;
        }).collect(Collectors.toList());
    }
}
