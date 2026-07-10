package com.workforcex.employer.ui.employer;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.workforcex.employer.api.RetrofitClient;
import com.workforcex.employer.api.Verification;
import com.workforcex.employer.databinding.ActivityVerifyWorkerBinding;
import com.workforcex.employer.utils.TokenManager;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyWorkerActivity extends AppCompatActivity {

    private ActivityVerifyWorkerBinding binding;
    private TokenManager tokenManager;
    private String workerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyWorkerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tokenManager = new TokenManager(this);

        workerId = getIntent().getStringExtra("workerId");
        String workerName = getIntent().getStringExtra("workerName");
        setTitle("Verify " + workerName);

        binding.rvDocuments.setLayoutManager(new LinearLayoutManager(this));
        loadWorkerDocuments();
    }

    private void loadWorkerDocuments() {
        binding.progressBar.setVisibility(View.VISIBLE);
        // This endpoint doesn't exist yet, so we'll need to add it
        RetrofitClient.get().getWorkerDocuments(tokenManager.getBearerToken(), workerId)
                .enqueue(new Callback<List<Verification>>() {
                    @Override
                    public void onResponse(Call<List<Verification>> call, Response<List<Verification>> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            binding.rvDocuments.setAdapter(new DocumentAdapter(response.body(), VerifyWorkerActivity.this::updateVerificationStatus));
                        } else {
                            Toast.makeText(VerifyWorkerActivity.this, "Failed to load documents", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Verification>> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(VerifyWorkerActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateVerificationStatus(String verificationId, String status, String comments) {
        RetrofitClient.get().updateEmployerVerificationStatus(tokenManager.getBearerToken(), verificationId, status, comments)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(VerifyWorkerActivity.this, "Verification status updated", Toast.LENGTH_SHORT).show();
                            loadWorkerDocuments(); // Refresh the list
                        } else {
                            Toast.makeText(VerifyWorkerActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(VerifyWorkerActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
