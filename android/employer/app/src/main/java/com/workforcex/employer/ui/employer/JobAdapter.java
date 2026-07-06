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
    private final OnJobAction onViewApplicants;
    private final OnJobAction onEdit;

    public JobAdapter(List<JobResponse> jobs, OnJobAction onFindCandidates,
                      OnJobAction onViewApplicants, OnJobAction onEdit) {
        this.jobs = jobs;
        this.onFindCandidates = onFindCandidates;
        this.onViewApplicants = onViewApplicants;
        this.onEdit = onEdit;
    }

    public void updateJobs(List<JobResponse> newJobs) {
        this.jobs = newJobs;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder h, int position) {
        JobResponse job = jobs.get(position);
        h.tvTitle.setText(job.title);
        h.tvLocation.setText("📍 " + (job.location != null ? job.location : "Not specified"));
        h.tvSkills.setText("Skills: " + (job.skillsRequired != null ? job.skillsRequired : "Any"));

        String salaryText = "";
        if (job.salaryMin != null && job.salaryMax != null)
            salaryText = "₹" + job.salaryMin.intValue() + " - ₹" + job.salaryMax.intValue() + "/month";
        if (job.openPositions != null && job.openPositions > 0)
            salaryText += (salaryText.isEmpty() ? "" : "  ·  ") + job.openPositions + " openings";
        h.tvSalary.setText(salaryText);

        h.btnFindCandidates.setOnClickListener(v -> onFindCandidates.onAction(job));
        h.btnApplicants.setOnClickListener(v -> onViewApplicants.onAction(job));
        h.btnEdit.setOnClickListener(v -> onEdit.onAction(job));
    }

    @Override public int getItemCount() { return jobs.size(); }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLocation, tvSkills, tvSalary;
        Button btnFindCandidates, btnApplicants, btnEdit;

        JobViewHolder(View v) {
            super(v);
            tvTitle          = v.findViewById(R.id.tvTitle);
            tvLocation       = v.findViewById(R.id.tvLocation);
            tvSkills         = v.findViewById(R.id.tvSkills);
            tvSalary         = v.findViewById(R.id.tvSalary);
            btnFindCandidates = v.findViewById(R.id.btnFindCandidates);
            btnApplicants    = v.findViewById(R.id.btnApplicants);
            btnEdit          = v.findViewById(R.id.btnEdit);
        }
    }
}
