package com.workforcex.employer.ui.employer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.workforcex.employer.api.Notification;
import com.workforcex.employer.api.RetrofitClient;
import com.workforcex.employer.databinding.ActivityNotificationsBinding;
import com.workforcex.employer.utils.TokenManager;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity {

    private ActivityNotificationsBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tokenManager = new TokenManager(this);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Notifications");

        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        loadNotifications();
    }

    private void loadNotifications() {
        RetrofitClient.get().getUnreadNotifications(tokenManager.getBearerToken())
                .enqueue(new Callback<List<Notification>>() {
                    @Override
                    public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            binding.rvNotifications.setAdapter(new NotificationAdapter(response.body(), NotificationsActivity.this::onNotificationClicked));
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Notification>> call, Throwable t) {
                        Toast.makeText(NotificationsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onNotificationClicked(Notification notification) {
        if ("JOB_APPLICANTS".equals(notification.linkType) && notification.linkId != null) {
            Intent intent = new Intent(this, JobApplicantsActivity.class);
            intent.putExtra("jobId", notification.linkId);
            // We don't have the job title here, so the title will be generic
            intent.putExtra("jobTitle", "Applicants");
            startActivity(intent);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    interface OnNotificationClick { void onNotification(Notification notification); }

    static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {
        private final List<Notification> items;
        private final OnNotificationClick onClick;

        NotificationAdapter(List<Notification> items, OnNotificationClick onClick) {
            this.items = items;
            this.onClick = onClick;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Notification item = items.get(pos);
            h.text.setText(item.message);
            h.itemView.setOnClickListener(v -> onClick.onNotification(item));
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView text;
            VH(View v) { super(v); text = v.findViewById(android.R.id.text1); }
        }
    }
}
