package com.workforcex.employer.ui.employer;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.workforcex.employer.api.MatchedWorker;
import com.workforcex.employer.api.RetrofitClient;
import com.workforcex.employer.databinding.ActivityMatchingResultsBinding;
import com.workforcex.employer.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class MatchingResultsActivity extends AppCompatActivity {

    private ActivityMatchingResultsBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMatchingResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tokenManager = new TokenManager(this);

        String jobId = getIntent().getStringExtra("jobId");
        String jobTitle = getIntent().getStringExtra("jobTitle");

        setTitle("Candidates: " + jobTitle);
        binding.tvJobTitle.setText("Ranked candidates for: " + jobTitle);

        binding.rvCandidates.setLayoutManager(new LinearLayoutManager(this));

        loadCandidates(jobId);
    }

    private void loadCandidates(String jobId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.get().getMatchedWorkers(tokenManager.getBearerToken(), jobId)
                .enqueue(new Callback<List<MatchedWorker>>() {
                    @Override
                    public void onResponse(Call<List<MatchedWorker>> call, Response<List<MatchedWorker>> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            List<MatchedWorker> workers = response.body();
                            binding.tvEmpty.setVisibility(workers.isEmpty() ? View.VISIBLE : View.GONE);
                            binding.rvCandidates.setAdapter(new CandidateAdapter(workers));
                        } else {
                            Toast.makeText(MatchingResultsActivity.this, "Failed to load candidates", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<MatchedWorker>> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(MatchingResultsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
