package com.workforcex.worker.ui.worker;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.workforcex.worker.R;
import com.workforcex.worker.api.JobApplication;
import com.workforcex.worker.api.RetrofitClient;
import com.workforcex.worker.databinding.ActivityMyApplicationsBinding;
import com.workforcex.worker.utils.TokenManager;
import java.net.URLEncoder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class MyApplicationsActivity extends AppCompatActivity {

    private ActivityMyApplicationsBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyApplicationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("My Applications");
        tokenManager = new TokenManager(this);
        binding.rvApplications.setLayoutManager(new LinearLayoutManager(this));
        loadApplications();
    }

    private void loadApplications() {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.get().getMyApplications(tokenManager.getBearerToken())
                .enqueue(new Callback<List<JobApplication>>() {
                    @Override
                    public void onResponse(Call<List<JobApplication>> call,
                                           Response<List<JobApplication>> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            List<JobApplication> apps = response.body();
                            binding.tvEmpty.setVisibility(apps.isEmpty() ? View.VISIBLE : View.GONE);
                            binding.rvApplications.setAdapter(new ApplicationsAdapter(apps));
                        }
                    }
                    @Override
                    public void onFailure(Call<List<JobApplication>> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(MyApplicationsActivity.this,
                                "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    static class ApplicationsAdapter extends RecyclerView.Adapter<ApplicationsAdapter.VH> {
        private final List<JobApplication> items;
        ApplicationsAdapter(List<JobApplication> items) { this.items = items; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_application, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            JobApplication app = items.get(pos);
            h.tvJobTitle.setText(app.jobTitle != null ? app.jobTitle : "Job");
            h.tvCompany.setText(app.companyName != null ? app.companyName : "");
            h.tvAppliedAt.setText(app.appliedAt != null
                    ? "Applied: " + app.appliedAt.substring(0, 10) : "");
            h.tvStatus.setText(app.status != null ? app.status : "PENDING");
            switch (app.status != null ? app.status : "PENDING") {
                case "SHORTLISTED":
                    h.tvStatus.setTextColor(Color.parseColor("#1B5E20")); break;
                case "REJECTED":
                    h.tvStatus.setTextColor(Color.parseColor("#B71C1C")); break;
                case "INTERVIEW":
                    h.tvStatus.setTextColor(Color.parseColor("#FFC107")); break;
                case "OFFERED":
                    h.tvStatus.setTextColor(Color.parseColor("#FF9800")); break;
                case "HIRED":
                    h.tvStatus.setTextColor(Color.parseColor("#4CAF50")); break;
                default:
                    h.tvStatus.setTextColor(Color.parseColor("#1565C0"));
            }

            if ("OFFERED".equals(app.status)) {
                h.contactLayout.setVisibility(View.VISIBLE);

                h.btnCall.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + app.employerMobile));
                    v.getContext().startActivity(intent);
                });

                h.btnWhatsApp.setOnClickListener(v -> {
                    try {
                        String message = URLEncoder.encode(
                            "Hello, I'm interested in the job offer for the '" + app.jobTitle + "' position.",
                            "UTF-8"
                        );
                        String url = "https://wa.me/+91" + app.employerMobile + "?text=" + message;
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        v.getContext().startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(v.getContext(), "WhatsApp not installed or error.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                h.contactLayout.setVisibility(View.GONE);
            }
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvJobTitle, tvCompany, tvAppliedAt, tvStatus;
            LinearLayout contactLayout;
            Button btnCall, btnWhatsApp;
            VH(View v) {
                super(v);
                tvJobTitle  = v.findViewById(R.id.tvJobTitle);
                tvCompany   = v.findViewById(R.id.tvCompany);
                tvAppliedAt = v.findViewById(R.id.tvAppliedAt);
                tvStatus    = v.findViewById(R.id.tvStatus);
                contactLayout = v.findViewById(R.id.contactLayout);
                btnCall = v.findViewById(R.id.btnCall);
                btnWhatsApp = v.findViewById(R.id.btnWhatsApp);
            }
        }
    }
}
