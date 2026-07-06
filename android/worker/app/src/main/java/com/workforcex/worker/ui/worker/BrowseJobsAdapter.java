package com.workforcex.worker.ui.worker;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.workforcex.worker.R;
import com.workforcex.worker.api.JobBrowseItem;
import java.util.List;
import java.util.Set;

public class BrowseJobsAdapter extends RecyclerView.Adapter<BrowseJobsAdapter.JobViewHolder> {

    public interface OnApplyClick { void onApply(JobBrowseItem job, int position); }

    private List<JobBrowseItem> jobs;
    private final OnApplyClick onApplyClick;
    private final Set<String> appliedJobIds;

    public BrowseJobsAdapter(List<JobBrowseItem> jobs, OnApplyClick onApplyClick, Set<String> appliedJobIds) {
        this.jobs = jobs;
        this.onApplyClick = onApplyClick;
        this.appliedJobIds = appliedJobIds;
    }

    public void update(List<JobBrowseItem> newJobs) {
        this.jobs = newJobs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_job_browse, parent, false);
        return new JobViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder h, int position) {
        JobBrowseItem job = jobs.get(position);
        h.tvTitle.setText(job.title);
        h.tvCompany.setText(job.companyName != null ? job.companyName : "");
        h.tvLocation.setText("📍 " + (job.location != null ? job.location : "Not specified"));
        h.tvSkills.setText("Skills: " + (job.skillsRequired != null ? job.skillsRequired : "Any"));

        if (job.salaryMin != null && job.salaryMax != null) {
            h.tvSalary.setText("₹" + job.salaryMin.intValue() + " – ₹" + job.salaryMax.intValue() + "/month");
        } else if (job.salaryMin != null) {
            h.tvSalary.setText("₹" + job.salaryMin.intValue() + "+/month");
        } else {
            h.tvSalary.setText("");
        }

        h.tvOpenings.setText(job.openPositions != null && job.openPositions > 0
                ? job.openPositions + " opening" + (job.openPositions > 1 ? "s" : "") : "");

        boolean isApplied = appliedJobIds.contains(job.id) || job.applied;
        if (isApplied) {
            h.btnApply.setEnabled(false);
            h.btnApply.setText("Applied");
        } else {
            h.btnApply.setEnabled(true);
            h.btnApply.setText("Apply Now");
        }

        h.btnApply.setOnClickListener(v -> onApplyClick.onApply(job, h.getAdapterPosition()));

        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), JobDetailsActivity.class);
            intent.putExtra("job", job);
            intent.putExtra("isApplied", isApplied);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return jobs.size(); }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCompany, tvLocation, tvSkills, tvSalary, tvOpenings;
        Button btnApply;

        JobViewHolder(View v) {
            super(v);
            tvTitle    = v.findViewById(R.id.tvTitle);
            tvCompany  = v.findViewById(R.id.tvCompany);
            tvLocation = v.findViewById(R.id.tvLocation);
            tvSkills   = v.findViewById(R.id.tvSkills);
            tvSalary   = v.findViewById(R.id.tvSalary);
            tvOpenings = v.findViewById(R.id.tvOpenings);
            btnApply   = v.findViewById(R.id.btnApply);
        }
    }
}
