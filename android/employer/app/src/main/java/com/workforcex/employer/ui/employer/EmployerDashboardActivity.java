package com.workforcex.employer.ui.employer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.workforcex.employer.api.EmployerProfileResponse;
import com.workforcex.employer.api.RetrofitClient;
import com.workforcex.employer.databinding.ActivityEmployerDashboardBinding;
import com.workforcex.employer.ui.auth.LoginActivity;
import com.workforcex.employer.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployerDashboardActivity extends AppCompatActivity {

    private ActivityEmployerDashboardBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmployerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tokenManager = new TokenManager(this);

        binding.btnMyJobs.setOnClickListener(v ->
                startActivity(new Intent(this, JobsActivity.class)));

        binding.btnSearchCandidates.setOnClickListener(v ->
                startActivity(new Intent(this, SearchCandidatesActivity.class)));

        binding.btnLogout.setOnClickListener(v -> logout());

        loadProfile();
    }

    private void loadProfile() {
        RetrofitClient.get().getEmployerProfile(tokenManager.getBearerToken())
                .enqueue(new Callback<EmployerProfileResponse>() {
                    @Override
                    public void onResponse(Call<EmployerProfileResponse> call, Response<EmployerProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            EmployerProfileResponse p = response.body();
                            String company = p.companyName != null ? p.companyName : tokenManager.getMobile();
                            binding.tvWelcome.setText("Welcome, " + company);
                        } else {
                            binding.tvWelcome.setText("Welcome, " + tokenManager.getMobile());
                        }
                    }
                    @Override
                    public void onFailure(Call<EmployerProfileResponse> call, Throwable t) {
                        binding.tvWelcome.setText("Welcome, " + tokenManager.getMobile());
                    }
                });
    }

    private void logout() {
        tokenManager.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
