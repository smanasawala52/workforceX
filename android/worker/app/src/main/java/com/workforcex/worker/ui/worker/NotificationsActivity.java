package com.workforcex.worker.ui.worker;

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
import com.workforcex.worker.api.Notification;
import com.workforcex.worker.api.RetrofitClient;
import com.workforcex.worker.databinding.ActivityNotificationsBinding;
import com.workforcex.worker.utils.TokenManager;
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
                            binding.tvEmpty.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
                            binding.rvNotifications.setAdapter(new NotificationAdapter(notifications));
                        } else {
                            binding.tvEmpty.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Notification>> call, Throwable t) {
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                        Toast.makeText(NotificationsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {
        private final List<Notification> items;
        NotificationAdapter(List<Notification> items) { this.items = items; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            h.text.setText(items.get(pos).message);
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView text;
            VH(View v) {
                super(v);
                text = v.findViewById(android.R.id.text1);
            }
        }
    }
}
