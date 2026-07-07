package com.workforcex.worker.ui.worker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.workforcex.worker.R;
import com.workforcex.worker.api.Notification;
import com.workforcex.worker.api.RetrofitClient;
import com.workforcex.worker.api.WorkerProfileResponse;
import com.workforcex.worker.databinding.ActivityWorkerDashboardBinding;
import com.workforcex.worker.utils.TokenManager;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkerDashboardActivity extends AppCompatActivity {

    private ActivityWorkerDashboardBinding binding;
    private TokenManager tokenManager;
    private TextView notificationBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorkerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tokenManager = new TokenManager(this);

        setSupportActionBar(binding.toolbar);
        setTitle("Worker Dashboard");

        binding.btnBrowseJobs.setOnClickListener(v -> startActivity(new Intent(this, BrowseJobsActivity.class)));
        binding.btnMyApplications.setOnClickListener(v -> startActivity(new Intent(this, MyApplicationsActivity.class)));
        binding.btnEditProfile.setOnClickListener(v -> startActivity(new Intent(this, WorkerProfileActivity.class)));
        binding.btnVerification.setOnClickListener(v -> startActivity(new Intent(this, VerificationActivity.class)));
        binding.btnLogout.setOnClickListener(v -> {
            tokenManager.clear();
            Intent intent = new Intent(this, com.workforcex.worker.ui.auth.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
        loadNotificationCount();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_notifications);
        View actionView = menuItem.getActionView();
        notificationBadge = actionView.findViewById(R.id.notification_badge);
        actionView.setOnClickListener(v -> onOptionsItemSelected(menuItem));
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
        RetrofitClient.get().getWorkerProfile(tokenManager.getBearerToken())
                .enqueue(new Callback<WorkerProfileResponse>() {
                    @Override
                    public void onResponse(Call<WorkerProfileResponse> call, Response<WorkerProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            WorkerProfileResponse p = response.body();
                            binding.tvWelcome.setText("Welcome, " + (p.name != null ? p.name : tokenManager.getMobile()));
                            binding.tvSkills.setText("Skills: " + (p.skills != null ? p.skills : "Not set"));
                            binding.tvExperience.setText("Experience: " + (p.experience != null ? p.experience + " yrs" : "Not set"));
                        }
                    }
                    @Override
                    public void onFailure(Call<WorkerProfileResponse> call, Throwable t) {}
                });
    }

    private void loadNotificationCount() {
        RetrofitClient.get().getUnreadNotifications(tokenManager.getBearerToken())
                .enqueue(new Callback<List<Notification>>() {
                    @Override
                    public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                        if (response.isSuccessful() && response.body() != null && notificationBadge != null) {
                            int count = response.body().size();
                            if (count > 0) {
                                notificationBadge.setText(String.valueOf(count));
                                notificationBadge.setVisibility(View.VISIBLE);
                            } else {
                                notificationBadge.setVisibility(View.GONE);
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Notification>> call, Throwable t) {}
                });
    }
}
