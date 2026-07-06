package com.workforcex.employer.ui.employer;

import android.content.Intent;
import android.net.Uri;
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
import com.workforcex.employer.api.MatchedWorker;
import com.workforcex.employer.api.RetrofitClient;
import com.workforcex.employer.databinding.ActivityMatchingResultsBinding;
import com.workforcex.employer.utils.TokenManager;
import java.net.URLEncoder;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchingResultsActivity extends AppCompatActivity {

    private ActivityMatchingResultsBinding binding;
    private TokenManager tokenManager;
    private String jobId;
    private String jobTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMatchingResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tokenManager = new TokenManager(this);

        this.jobId = getIntent().getStringExtra("jobId");
        this.jobTitle = getIntent().getStringExtra("jobTitle");

        setTitle("Candidates: " + jobTitle);
        binding.tvJobTitle.setText("Ranked candidates for: " + jobTitle);

        binding.rvCandidates.setLayoutManager(new LinearLayoutManager(this));

        loadCandidates(this.jobId);
    }

    private void loadCandidates(String jobId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.get().getMatchedWorkers(tokenManager.getBearerToken(), jobId)
                .enqueue(new Callback<List<MatchedWorker>>() {
                    @Override
                    public void onResponse(Call<List<MatchedWorker>> call, Response<List<MatchedWorker>> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            List<MatchedWorker> workers = response.body();
                            binding.tvEmpty.setVisibility(workers.isEmpty() ? View.VISIBLE : View.GONE);
                            binding.rvCandidates.setAdapter(new CandidateAdapter(workers, MatchingResultsActivity.this::offerJob, jobTitle));
                        } else {
                            Toast.makeText(MatchingResultsActivity.this, "Failed to load candidates", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<MatchedWorker>> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(MatchingResultsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void offerJob(MatchedWorker worker, Button button) {
        button.setEnabled(false);
        button.setText("Offering...");

        RetrofitClient.get().offerJob(tokenManager.getBearerToken(), this.jobId, worker.workerId)
                .enqueue(new Callback<JobApplicationItem>() {
                    @Override
                    public void onResponse(Call<JobApplicationItem> call, Response<JobApplicationItem> response) {
                        if (response.isSuccessful()) {
                            button.setText("Offered");
                            Toast.makeText(MatchingResultsActivity.this, "Offer sent to " + worker.name, Toast.LENGTH_SHORT).show();
                        } else {
                            button.setEnabled(true);
                            button.setText("Offer Job");
                            Toast.makeText(MatchingResultsActivity.this, "Failed to send offer. The worker may have already applied.", Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<JobApplicationItem> call, Throwable t) {
                        button.setEnabled(true);
                        button.setText("Offer Job");
                        Toast.makeText(MatchingResultsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    static class CandidateAdapter extends RecyclerView.Adapter<CandidateAdapter.VH> {
        private final List<MatchedWorker> items;
        private final OnOfferClick onOfferClick;
        private final String jobTitle;

        interface OnOfferClick { void onOffer(MatchedWorker worker, Button button); }

        CandidateAdapter(List<MatchedWorker> items, OnOfferClick onOfferClick, String jobTitle) {
            this.items = items;
            this.onOfferClick = onOfferClick;
            this.jobTitle = jobTitle;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_candidate, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            MatchedWorker item = items.get(pos);
            h.tvRank.setText(String.valueOf(pos + 1));
            h.tvName.setText(item.name);
            h.tvScore.setText(String.format("%.0f%% Match", item.score));
            h.tvSkills.setText("Skills: " + item.skills);
            h.tvExperience.setText("Experience: " + item.experience + " yrs");
            h.tvCity.setText("City: " + item.city);
            h.tvSalary.setText("Prefers: ₹" + item.preferredSalary.intValue());
            h.tvMobile.setText("📞 " + item.mobileNumber);

            boolean hasBeenContacted = item.applicationStatus != null;
            if (hasBeenContacted) {
                h.btnOfferJob.setEnabled(false);
                h.btnOfferJob.setText(item.applicationStatus);
            } else {
                h.btnOfferJob.setEnabled(true);
                h.btnOfferJob.setText("Offer Job");
            }
            h.btnOfferJob.setOnClickListener(v -> onOfferClick.onOffer(item, h.btnOfferJob));

            h.btnCall.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + item.mobileNumber));
                v.getContext().startActivity(intent);
            });

            h.btnWhatsApp.setOnClickListener(v -> {
                try {
                    String message = URLEncoder.encode(
                        "Hello " + item.name + ", we have a job offer for you for the position of '" + this.jobTitle + "'. Are you interested?",
                        "UTF-8"
                    );
                    String url = "https://wa.me/+91" + item.mobileNumber + "?text=" + message;
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "WhatsApp not installed or error.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvRank, tvName, tvScore, tvSkills, tvExperience, tvCity, tvSalary, tvMobile;
            Button btnOfferJob, btnCall, btnWhatsApp;
            VH(View v) {
                super(v);
                tvRank = v.findViewById(R.id.tvRank);
                tvName = v.findViewById(R.id.tvName);
                tvScore = v.findViewById(R.id.tvScore);
                tvSkills = v.findViewById(R.id.tvSkills);
                tvExperience = v.findViewById(R.id.tvExperience);
                tvCity = v.findViewById(R.id.tvCity);
                tvSalary = v.findViewById(R.id.tvSalary);
                tvMobile = v.findViewById(R.id.tvMobile);
                btnOfferJob = v.findViewById(R.id.btnOfferJob);
                btnCall = v.findViewById(R.id.btnCall);
                btnWhatsApp = v.findViewById(R.id.btnWhatsApp);
            }
        }
    }
}
