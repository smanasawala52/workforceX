package com.workforcex.employer.ui.employer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.workforcex.employer.R;
import com.workforcex.employer.api.EmployerProfileResponse;
import com.workforcex.employer.api.Notification;
import com.workforcex.employer.api.RetrofitClient;
import com.workforcex.employer.databinding.ActivityEmployerDashboardBinding;
import com.workforcex.employer.ui.auth.LoginActivity;
import com.workforcex.employer.utils.TokenManager;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployerDashboardActivity extends AppCompatActivity {

    private ActivityEmployerDashboardBinding binding;
    private TokenManager tokenManager;
    private TextView badge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmployerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tokenManager = new TokenManager(this);

        setSupportActionBar(binding.toolbar);

        binding.btnMyJobs.setOnClickListener(v ->
                startActivity(new Intent(this, JobsActivity.class)));

        binding.btnSearchCandidates.setOnClickListener(v ->
                startActivity(new Intent(this, SearchCandidatesActivity.class)));

        binding.btnLogout.setOnClickListener(v -> logout());

        loadProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh notification count when returning to the screen
        if (badge != null) {
            loadNotificationCount();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_notifications);
        View actionView = menuItem.getActionView();
        badge = actionView.findViewById(R.id.badge);

        actionView.setOnClickListener(v -> onOptionsItemSelected(menuItem));

        loadNotificationCount();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            startActivity(new Intent(this, NotificationsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void loadNotificationCount() {
        RetrofitClient.get().getUnreadNotifications(tokenManager.getBearerToken())
                .enqueue(new Callback<List<Notification>>() {
                    @Override
                    public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            int count = response.body().size();
                            if (badge != null) {
                                if (count > 0) {
                                    badge.setText(String.valueOf(count));
                                    badge.setVisibility(View.VISIBLE);
                                } else {
                                    badge.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Notification>> call, Throwable t) {}
                });
    }

    private void logout() {
        tokenManager.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
