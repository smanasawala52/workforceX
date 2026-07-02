package com.workforcex.worker.ui.worker;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.workforcex.worker.api.JobBrowseItem;
import com.workforcex.worker.api.RetrofitClient;
import com.workforcex.worker.databinding.ActivityBrowseJobsBinding;
import com.workforcex.worker.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class BrowseJobsActivity extends AppCompatActivity {

    private ActivityBrowseJobsBinding binding;
    private TokenManager tokenManager;
    private BrowseJobsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrowseJobsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Available Jobs");

        tokenManager = new TokenManager(this);
        adapter = new BrowseJobsAdapter(new ArrayList<>());
        binding.rvJobs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvJobs.setAdapter(adapter);

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
                            List<JobBrowseItem> jobs = response.body();
                            adapter.update(jobs);
                            binding.tvEmpty.setVisibility(
                                    jobs.isEmpty() ? View.VISIBLE : View.GONE);
                        } else {
                            Toast.makeText(BrowseJobsActivity.this,
                                    "Failed to load jobs", Toast.LENGTH_SHORT).show();
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
}
