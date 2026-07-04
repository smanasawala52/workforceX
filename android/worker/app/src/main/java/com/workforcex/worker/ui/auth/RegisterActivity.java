package com.workforcex.worker.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.workforcex.worker.api.RegisterRequest;
import com.workforcex.worker.api.RegisterResponse;
import com.workforcex.worker.api.RetrofitClient;
import com.workforcex.worker.databinding.ActivityRegisterBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String mobile = binding.etMobile.getText().toString().trim();
        if (mobile.length() != 10) {
            binding.etMobile.setError("Enter a valid 10-digit mobile number");
            return;
        }
        setLoading(true);
        // Worker app always registers as WORKER
        RetrofitClient.get().register(new RegisterRequest(mobile, "WORKER", "+91"))
                .enqueue(new Callback<RegisterResponse>() {
                    @Override
                    public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Account created! Please login.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Mobile number already registered", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<RegisterResponse> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!loading);
    }
}
