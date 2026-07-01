package com.workforcex.employer.ui.employer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.workforcex.employer.R;
import com.workforcex.employer.api.JobResponse;
import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    public interface OnJobAction { void onAction(JobResponse job); }

    private List<JobResponse> jobs;
    private final OnJobAction onFindCandidates;
    private final OnJobAction onEdit;

    public JobAdapter(List<JobResponse> jobs, OnJobAction onFindCandidates, OnJobAction onEdit) {
        this.jobs = jobs;
        this.onFindCandidates = onFindCandidates;
        this.onEdit = onEdit;
    }

    public void updateJobs(List<JobResponse> newJobs) {
        this.jobs = newJobs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        JobResponse job = jobs.get(position);
        holder.tvTitle.setText(job.title);
        holder.tvLocation.setText("📍 " + (job.location != null ? job.location : "Not specified"));
        holder.tvSkills.setText("Skills: " + (job.skillsRequired != null ? job.skillsRequired : "Any"));
        String salaryText = "";
        if (job.salaryMin != null && job.salaryMax != null) {
            salaryText = "₹" + job.salaryMin.intValue() + " - ₹" + job.salaryMax.intValue() + "/month";
        } else if (job.salaryMin != null) {
            salaryText = "₹" + job.salaryMin.intValue() + "+/month";
        }
        if (job.openPositions != null && job.openPositions > 0) {
            salaryText += (salaryText.isEmpty() ? "" : "  ·  ") + job.openPositions + " openings";
        }
        holder.tvSalary.setText(salaryText);
        holder.btnFindCandidates.setOnClickListener(v -> onFindCandidates.onAction(job));
        holder.btnEdit.setOnClickListener(v -> onEdit.onAction(job));
    }

    @Override
    public int getItemCount() { return jobs.size(); }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLocation, tvSkills, tvSalary;
        Button btnFindCandidates, btnEdit;

        JobViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvSkills = itemView.findViewById(R.id.tvSkills);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            btnFindCandidates = itemView.findViewById(R.id.btnFindCandidates);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}
