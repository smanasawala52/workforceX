package com.workforcex.worker.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.workforcex.worker.api.LoginRequest;
import com.workforcex.worker.api.LoginResponse;
import com.workforcex.worker.api.RetrofitClient;
import com.workforcex.worker.databinding.ActivityLoginBinding;
import com.workforcex.worker.ui.worker.WorkerDashboardActivity;
import com.workforcex.worker.utils.TokenManager;
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
        if (mobile.length() != 10) { binding.etMobile.setError("Enter a valid 10-digit mobile number"); return; }
        if (password.isEmpty()) { binding.etPassword.setError("Password is required"); return; }
        setLoading(true);
        RetrofitClient.get().login(new LoginRequest(mobile, password))
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse body = response.body();
                            if (!"WORKER".equals(body.role)) {
                                Toast.makeText(LoginActivity.this, "Please use the Employer app to login as an Employer", Toast.LENGTH_LONG).show();
                                return;
                            }
                            tokenManager.save(body.token, body.role, body.mobileNumber);
                            Intent intent = new Intent(LoginActivity.this, WorkerDashboardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
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

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
    }
}
