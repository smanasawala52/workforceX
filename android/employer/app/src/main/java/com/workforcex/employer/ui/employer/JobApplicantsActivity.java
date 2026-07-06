package com.workforcex.employer.ui.employer;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.workforcex.employer.R;
import com.workforcex.employer.api.JobApplicationItem;
import com.workforcex.employer.api.RetrofitClient;
import com.workforcex.employer.databinding.ActivityJobApplicantsBinding;
import com.workforcex.employer.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class JobApplicantsActivity extends AppCompatActivity {

    private ActivityJobApplicantsBinding binding;
    private TokenManager tokenManager;
    private String jobId;
    private ApplicantAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJobApplicantsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tokenManager = new TokenManager(this);

        jobId = getIntent().getStringExtra("jobId");
        String jobTitle = getIntent().getStringExtra("jobTitle");
        setTitle("Applicants");
        binding.tvTitle.setText("Applicants for: " + jobTitle);
        binding.rvApplicants.setLayoutManager(new LinearLayoutManager(this));
        loadApplicants();
    }

    private void loadApplicants() {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.get().getApplicationsForJob(tokenManager.getBearerToken(), jobId)
                .enqueue(new Callback<List<JobApplicationItem>>() {
                    @Override
                    public void onResponse(Call<List<JobApplicationItem>> call,
                                           Response<List<JobApplicationItem>> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            List<JobApplicationItem> items = response.body();
                            binding.tvCount.setText(items.size() + " applicant(s)");
                            binding.tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                            adapter = new ApplicantAdapter(items, JobApplicantsActivity.this::updateStatus);
                            binding.rvApplicants.setAdapter(adapter);
                        }
                    }
                    @Override
                    public void onFailure(Call<List<JobApplicationItem>> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(JobApplicantsActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateStatus(JobApplicationItem item, String status, int position) {
        RetrofitClient.get().updateApplicationStatus(
                tokenManager.getBearerToken(), item.applicationId, status)
                .enqueue(new Callback<JobApplicationItem>() {
                    @Override
                    public void onResponse(Call<JobApplicationItem> call,
                                           Response<JobApplicationItem> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            item.status = response.body().status;
                            adapter.notifyItemChanged(position);
                            Toast.makeText(JobApplicantsActivity.this,
                                    (item.workerName != null ? item.workerName : "Applicant")
                                            + " " + status.toLowerCase() + "d", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<JobApplicationItem> call, Throwable t) {
                        Toast.makeText(JobApplicantsActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    interface OnStatusAction { void onAction(JobApplicationItem item, String status, int position); }

    static class ApplicantAdapter extends RecyclerView.Adapter<ApplicantAdapter.VH> {
        private final List<JobApplicationItem> items;
        private final OnStatusAction onAction;

        ApplicantAdapter(List<JobApplicationItem> items, OnStatusAction onAction) {
            this.items = items;
            this.onAction = onAction;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_applicant, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            JobApplicationItem item = items.get(pos);

            h.tvName.setText(item.workerName != null ? item.workerName : item.workerMobile);
            h.tvMobile.setText("📞 " + (item.workerMobile != null ? item.workerMobile : ""));
            h.tvSkills.setText("Skills: " + (item.workerSkills != null ? item.workerSkills : "—"));

            String details = "";
            if (item.workerExperience != null) details += item.workerExperience + " yrs";
            if (item.workerCity != null) details += (details.isEmpty() ? "" : " · ") + item.workerCity;
            if (item.workerPreferredSalary != null)
                details += "  ₹" + item.workerPreferredSalary.intValue();
            h.tvDetails.setText(details);

            String status = item.status != null ? item.status : "PENDING";
            h.tvStatus.setText(status);
            switch (status) {
                case "SHORTLISTED":
                    h.tvStatus.setTextColor(Color.parseColor("#1B5E20")); break;
                case "REJECTED":
                    h.tvStatus.setTextColor(Color.parseColor("#B71C1C")); break;
                default:
                    h.tvStatus.setTextColor(Color.parseColor("#1565C0"));
            }

            boolean actionable = !"REJECTED".equals(status);
            h.btnShortlist.setEnabled("PENDING".equals(status));
            h.btnReject.setEnabled(actionable);

            h.btnShortlist.setOnClickListener(v ->
                    onAction.onAction(item, "SHORTLISTED", h.getAdapterPosition()));
            h.btnReject.setOnClickListener(v ->
                    onAction.onAction(item, "REJECTED", h.getAdapterPosition()));
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvMobile, tvSkills, tvDetails, tvStatus;
            Button btnShortlist, btnReject;
            VH(View v) {
                super(v);
                tvName      = v.findViewById(R.id.tvName);
                tvMobile    = v.findViewById(R.id.tvMobile);
                tvSkills    = v.findViewById(R.id.tvSkills);
                tvDetails   = v.findViewById(R.id.tvDetails);
                tvStatus    = v.findViewById(R.id.tvStatus);
                btnShortlist = v.findViewById(R.id.btnShortlist);
                btnReject    = v.findViewById(R.id.btnReject);
            }
        }
    }
}
