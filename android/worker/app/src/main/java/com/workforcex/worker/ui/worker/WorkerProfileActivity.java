package com.workforcex.worker.ui.worker;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.workforcex.worker.api.RetrofitClient;
import com.workforcex.worker.api.WorkerProfileRequest;
import com.workforcex.worker.api.WorkerProfileResponse;
import com.workforcex.worker.databinding.ActivityWorkerProfileBinding;
import com.workforcex.worker.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkerProfileActivity extends AppCompatActivity {

    private ActivityWorkerProfileBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorkerProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tokenManager = new TokenManager(this);

        loadExistingProfile();
        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadExistingProfile() {
        RetrofitClient.get().getWorkerProfile(tokenManager.getBearerToken())
                .enqueue(new Callback<WorkerProfileResponse>() {
                    @Override
                    public void onResponse(Call<WorkerProfileResponse> call, Response<WorkerProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            WorkerProfileResponse p = response.body();
                            if (p.name != null) binding.etName.setText(p.name);
                            if (p.city != null) binding.etCity.setText(p.city);
                            if (p.state != null) binding.etState.setText(p.state);
                            if (p.skills != null) binding.etSkills.setText(p.skills);
                            if (p.experience != null) binding.etExperience.setText(String.valueOf(p.experience));
                            if (p.preferredSalary != null) binding.etSalary.setText(String.valueOf(p.preferredSalary.intValue()));
                        }
                    }

                    @Override
                    public void onFailure(Call<WorkerProfileResponse> call, Throwable t) { /* no existing profile yet, that's fine */ }
                });
    }

    private void saveProfile() {
        WorkerProfileRequest request = new WorkerProfileRequest();
        request.name = binding.etName.getText().toString().trim();
        request.city = binding.etCity.getText().toString().trim();
        request.state = binding.etState.getText().toString().trim();
        request.skills = binding.etSkills.getText().toString().trim();

        String expStr = binding.etExperience.getText().toString().trim();
        request.experience = expStr.isEmpty() ? null : Integer.parseInt(expStr);

        String salStr = binding.etSalary.getText().toString().trim();
        request.preferredSalary = salStr.isEmpty() ? null : Double.parseDouble(salStr);

        setLoading(true);

        RetrofitClient.get().saveWorkerProfile(tokenManager.getBearerToken(), request)
                .enqueue(new Callback<WorkerProfileResponse>() {
                    @Override
                    public void onResponse(Call<WorkerProfileResponse> call, Response<WorkerProfileResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            Toast.makeText(WorkerProfileActivity.this, "Profile saved!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(WorkerProfileActivity.this, "Failed to save profile", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<WorkerProfileResponse> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(WorkerProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSave.setEnabled(!loading);
    }
}
