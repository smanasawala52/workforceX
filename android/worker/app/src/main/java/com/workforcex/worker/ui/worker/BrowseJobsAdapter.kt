package com.workforcex.worker.ui.worker

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.workforcex.worker.R
import com.workforcex.shared.models.JobBrowseItem

class BrowseJobsAdapter(
    private var jobs: List<JobBrowseItem>,
    private val onApplyClick: (JobBrowseItem, Int) -> Unit,
    private val appliedJobIds: Set<String>
) : RecyclerView.Adapter<BrowseJobsAdapter.JobViewHolder>() {

    fun update(newJobs: List<JobBrowseItem>) {
        this.jobs = newJobs
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job_browse, parent, false)
        return JobViewHolder(v)
    }

    override fun onBindViewHolder(h: JobViewHolder, position: Int) {
        val job = jobs[position]
        h.tvTitle.text = job.title
        h.tvCompany.text = job.companyName ?: ""
        h.tvLocation.text = "📍 " + (job.location ?: "Not specified")
        h.tvSkills.text = "Skills: " + (job.skillsRequired ?: "Any")

        if (job.salaryMin != null && job.salaryMax != null) {
            h.tvSalary.text = "₹" + job.salaryMin.toInt() + " – ₹" + job.salaryMax.toInt() + "/month"
        } else if (job.salaryMin != null) {
            h.tvSalary.text = "₹" + job.salaryMin.toInt() + "+/month"
        } else {
            h.tvSalary.text = ""
        }

        h.tvOpenings.text = if (job.openPositions != null && job.openPositions > 0)
            job.openPositions.toString() + " opening" + if (job.openPositions > 1) "s" else ""
        else ""

        val isApplied = appliedJobIds.contains(job.id) || job.applied
        if (isApplied) {
            h.btnApply.isEnabled = false
            h.btnApply.text = "Applied"
        } else {
            h.btnApply.isEnabled = true
            h.btnApply.text = "Apply Now"
        }

        h.btnApply.setOnClickListener { onApplyClick(job, h.adapterPosition) }

        h.itemView.setOnClickListener { v ->
            val intent = Intent(v.context, JobDetailsActivity::class.java)
            intent.putExtra("job", job)
            intent.putExtra("isApplied", isApplied)
            v.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = jobs.size

    class JobViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvTitle: TextView = v.findViewById(R.id.tvTitle)
        val tvCompany: TextView = v.findViewById(R.id.tvCompany)
        val tvLocation: TextView = v.findViewById(R.id.tvLocation)
        val tvSkills: TextView = v.findViewById(R.id.tvSkills)
        val tvSalary: TextView = v.findViewById(R.id.tvSalary)
        val tvOpenings: TextView = v.findViewById(R.id.tvOpenings)
        val btnApply: Button = v.findViewById(R.id.btnApply)
    }
}