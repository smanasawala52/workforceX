package com.workforcex.worker.ui.worker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.workforcex.worker.api.RetrofitClient;
import com.workforcex.worker.api.WorkerProfileResponse;
import com.workforcex.worker.databinding.ActivityWorkerDashboardBinding;
import com.workforcex.worker.ui.auth.LoginActivity;
import com.workforcex.worker.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkerDashboardActivity extends AppCompatActivity {

    private ActivityWorkerDashboardBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorkerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(this);

        binding.tvWelcome.setText("Welcome, " + tokenManager.getMobile());

        binding.btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, WorkerProfileActivity.class)));

        binding.btnLogout.setOnClickListener(v -> logout());

        loadProfile();
    }

    private void loadProfile() {
        RetrofitClient.get().getWorkerProfile(tokenManager.getBearerToken())
                .enqueue(new Callback<WorkerProfileResponse>() {
                    @Override
                    public void onResponse(Call<WorkerProfileResponse> call, Response<WorkerProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            WorkerProfileResponse profile = response.body();
                            String name = profile.name != null ? profile.name : tokenManager.getMobile();
                            binding.tvWelcome.setText("Welcome, " + name);
                            binding.tvSkills.setText("Skills: " + (profile.skills != null ? profile.skills : "Not set"));
                            binding.tvExperience.setText("Experience: " + (profile.experience != null ? profile.experience + " yrs" : "Not set"));
                        }
                    }

                    @Override
                    public void onFailure(Call<WorkerProfileResponse> call, Throwable t) {
                        Toast.makeText(WorkerDashboardActivity.this, "Could not load profile", Toast.LENGTH_SHORT).show();
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
