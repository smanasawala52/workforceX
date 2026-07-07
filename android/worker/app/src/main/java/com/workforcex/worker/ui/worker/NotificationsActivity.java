package com.workforcex.worker.ui.worker;

import android.content.Intent;
import android.graphics.Color;
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
import com.workforcex.worker.R;
import com.workforcex.worker.api.Notification;
import com.workforcex.worker.api.RetrofitClient;
import com.workforcex.worker.databinding.ActivityNotificationsBinding;
import com.workforcex.worker.utils.TokenManager;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
        setTitle("Notifications");
        tokenManager = new TokenManager(this);
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        loadNotifications();
    }

    private void loadNotifications() {
        binding.tvEmpty.setVisibility(View.GONE);
        RetrofitClient.get().getUnreadNotifications(tokenManager.getBearerToken())
                .enqueue(new Callback<List<Notification>>() {
                    @Override
                    public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Notification> notifications = response.body();
                            Collections.sort(notifications, (a, b) -> b.createdAt.compareTo(a.createdAt));
                            binding.rvNotifications.setAdapter(new NotificationAdapter(notifications, NotificationsActivity.this::onNotificationClicked));
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Notification>> call, Throwable t) {
                        Toast.makeText(NotificationsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onNotificationClicked(Notification notification) {
        if ("MY_APPLICATIONS".equals(notification.linkType)) {
            Intent intent = new Intent(this, MyApplicationsActivity.class);
            startActivity(intent);
        }
    }

    interface OnNotificationClick { void onNotification(Notification notification); }

    static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {
        private final List<Notification> items;
        private final OnNotificationClick onClick;
        private final DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        private final SimpleDateFormat outputFormatter = new SimpleDateFormat("dd-MMM-yyyy, hh:mm a", Locale.ENGLISH);

        NotificationAdapter(List<Notification> items, OnNotificationClick onClick) {
            this.items = items;
            this.onClick = onClick;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Notification item = items.get(pos);
            h.tvMessage.setText(item.message);

            try {
                LocalDateTime localDateTime = LocalDateTime.parse(item.createdAt, inputFormatter);
                Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                h.tvDate.setText(outputFormatter.format(date));
            } catch (Exception e) {
                h.tvDate.setText(item.createdAt != null ? item.createdAt.substring(0, 10) : "");
            }

            if (pos % 2 == 0) {
                h.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            } else {
                h.itemView.setBackgroundColor(Color.parseColor("#E3F2FD"));
            }

            h.itemView.setOnClickListener(v -> onClick.onNotification(item));
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvMessage, tvDate;
            VH(View v) {
                super(v);
                tvMessage = v.findViewById(R.id.tvMessage);
                tvDate = v.findViewById(R.id.tvDate);
            }
        }
    }
}
