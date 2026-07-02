package com.workforcex.worker.ui.worker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.workforcex.worker.R;
import com.workforcex.worker.api.JobBrowseItem;
import java.util.List;

public class BrowseJobsAdapter extends RecyclerView.Adapter<BrowseJobsAdapter.JobViewHolder> {

    private List<JobBrowseItem> jobs;

    public BrowseJobsAdapter(List<JobBrowseItem> jobs) { this.jobs = jobs; }

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
        h.tvSkills.setText("Skills needed: " + (job.skillsRequired != null ? job.skillsRequired : "Any"));

        // Salary range
        if (job.salaryMin != null && job.salaryMax != null) {
            h.tvSalary.setText("₹" + job.salaryMin.intValue() + " – ₹" + job.salaryMax.intValue() + "/month");
        } else if (job.salaryMin != null) {
            h.tvSalary.setText("₹" + job.salaryMin.intValue() + "+/month");
        } else {
            h.tvSalary.setText("Salary: Not specified");
        }

        if (job.openPositions != null && job.openPositions > 0) {
            h.tvOpenings.setText(job.openPositions + " opening" + (job.openPositions > 1 ? "s" : ""));
        } else {
            h.tvOpenings.setText("");
        }
    }

    @Override
    public int getItemCount() { return jobs.size(); }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCompany, tvLocation, tvSkills, tvSalary, tvOpenings;
        JobViewHolder(View v) {
            super(v);
            tvTitle    = v.findViewById(R.id.tvTitle);
            tvCompany  = v.findViewById(R.id.tvCompany);
            tvLocation = v.findViewById(R.id.tvLocation);
            tvSkills   = v.findViewById(R.id.tvSkills);
            tvSalary   = v.findViewById(R.id.tvSalary);
            tvOpenings = v.findViewById(R.id.tvOpenings);
        }
    }
}
