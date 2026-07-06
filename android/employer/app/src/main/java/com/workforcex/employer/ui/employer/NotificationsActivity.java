package com.workforcex.employer.ui.employer;

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
import com.workforcex.employer.R;
import com.workforcex.employer.api.Notification;
import com.workforcex.employer.api.RetrofitClient;
import com.workforcex.employer.databinding.ActivityNotificationsBinding;
import com.workforcex.employer.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private ActivityNotificationsBinding binding;
    private TokenManager tokenManager;
    private NotificationAdapter adapter;

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
                            List<Notification> notifications = response.body();
                            binding.tvEmpty.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
                            adapter = new NotificationAdapter(notifications);
                            binding.rvNotifications.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Notification>> call, Throwable t) {
                        Toast.makeText(NotificationsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {
        private final List<Notification> items;

        NotificationAdapter(List<Notification> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Notification item = items.get(pos);
            h.text.setText(item.message);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView text;

            VH(View v) {
                super(v);
                text = v.findViewById(android.R.id.text1);
            }
        }
    }
}
