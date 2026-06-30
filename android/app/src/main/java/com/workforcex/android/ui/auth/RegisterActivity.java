package com.workforcex.android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.workforcex.android.api.RegisterRequest;
import com.workforcex.android.api.RegisterResponse;
import com.workforcex.android.api.RetrofitClient;
import com.workforcex.android.databinding.ActivityRegisterBinding;
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
            binding.etMobile.setError(getString(R.string.error_mobile_required));
            return;
        }

        int selectedId = binding.radioGroupRole.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, getString(R.string.error_role_required), Toast.LENGTH_SHORT).show();
            return;
        }

        String role = (selectedId == R.id.rbWorker) ? "WORKER" : "EMPLOYER";
        setLoading(true);

        RetrofitClient.get().register(new RegisterRequest(mobile, role))
                .enqueue(new Callback<RegisterResponse>() {
                    @Override
                    public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Registered! Please login.", Toast.LENGTH_SHORT).show();
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
