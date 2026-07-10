package com.workforcex.employer.ui.employer;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.workforcex.employer.api.RetrofitClient;
import com.workforcex.employer.api.Verification;
import com.workforcex.employer.databinding.ActivityReviewVerificationsBinding;
import com.workforcex.employer.utils.TokenManager;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewVerificationsActivity extends AppCompatActivity {

    private ActivityReviewVerificationsBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewVerificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tokenManager = new TokenManager(this);

        setTitle("Review Verifications");
        binding.rvVerifications.setLayoutManager(new LinearLayoutManager(this));
        loadPendingVerifications();
    }

    private void loadPendingVerifications() {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.get().getPendingVerifications(tokenManager.getBearerToken())
                .enqueue(new Callback<List<Verification>>() {
                    @Override
                    public void onResponse(Call<List<Verification>> call, Response<List<Verification>> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            binding.rvVerifications.setAdapter(new VerificationAdapter(response.body(), ReviewVerificationsActivity.this::updateVerificationStatus));
                        } else {
                            Toast.makeText(ReviewVerificationsActivity.this, "Failed to load verifications", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Verification>> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(ReviewVerificationsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateVerificationStatus(String verificationId, String status, String comments) {
        RetrofitClient.get().updateEmployerVerificationStatus(tokenManager.getBearerToken(), verificationId, status, comments)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(ReviewVerificationsActivity.this, "Verification status updated", Toast.LENGTH_SHORT).show();
                            loadPendingVerifications(); // Refresh the list
                        } else {
                            Toast.makeText(ReviewVerificationsActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(ReviewVerificationsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
