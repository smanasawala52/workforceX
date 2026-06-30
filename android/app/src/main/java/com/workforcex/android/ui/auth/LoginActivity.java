package com.workforcex.android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.workforcex.android.api.LoginRequest;
import com.workforcex.android.api.LoginResponse;
import com.workforcex.android.api.RetrofitClient;
import com.workforcex.android.databinding.ActivityLoginBinding;
import com.workforcex.android.ui.employer.EmployerDashboardActivity;
import com.workforcex.android.ui.worker.WorkerDashboardActivity;
import com.workforcex.android.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(this);

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.tvGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String mobile = binding.etMobile.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (mobile.length() != 10) {
            binding.etMobile.setError("Enter a valid 10-digit mobile number");
            return;
        }
        if (password.isEmpty()) {
            binding.etPassword.setError("Password is required");
            return;
        }

        setLoading(true);

        RetrofitClient.get().login(new LoginRequest(mobile, password))
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse body = response.body();
                            tokenManager.save(body.token, body.role, body.mobileNumber);
                            navigateToDashboard(body.role);
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid mobile number or password", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToDashboard(String role) {
        Intent intent = "WORKER".equals(role)
                ? new Intent(this, WorkerDashboardActivity.class)
                : new Intent(this, EmployerDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
    }
}
