package com.workforcex.employer.ui.employer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.workforcex.employer.R
import com.workforcex.shared_employer.models.JobResponse

class JobAdapter(
    private var jobs: MutableList<JobResponse>,
    private val onFindCandidates: (JobResponse) -> Unit,
    private val onViewApplicants: (JobResponse) -> Unit,
    private val onEdit: (JobResponse) -> Unit
) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    fun updateJobs(newJobs: List<JobResponse>) {
        this.jobs.clear()
        this.jobs.addAll(newJobs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(h: JobViewHolder, position: Int) {
        val job = jobs[position]
        h.tvTitle.text = job.title
        h.tvLocation.text = "📍 " + (job.location ?: "Not specified")
        h.tvSkills.text = "Skills: " + (job.skillsRequired ?: "Any")

        var salaryText = ""
        if (job.salaryMin != null && job.salaryMax != null)
            salaryText = "₹" + job.salaryMin.toInt() + " - ₹" + job.salaryMax.toInt() + "/month"
        if (job.openPositions != null && job.openPositions > 0)
            salaryText += (if (salaryText.isEmpty()) "" else "  ·  ") + job.openPositions + " openings"
        h.tvSalary.text = salaryText

        h.btnFindCandidates.setOnClickListener { onFindCandidates(job) }
        h.btnApplicants.setOnClickListener { onViewApplicants(job) }
        h.btnEdit.setOnClickListener { onEdit(job) }
    }

    override fun getItemCount(): Int = jobs.size

    class JobViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvTitle: TextView = v.findViewById(R.id.tvTitle)
        val tvLocation: TextView = v.findViewById(R.id.tvLocation)
        val tvSkills: TextView = v.findViewById(R.id.tvSkills)
        val tvSalary: TextView = v.findViewById(R.id.tvSalary)
        val btnFindCandidates: Button = v.findViewById(R.id.btnFindCandidates)
        val btnApplicants: Button = v.findViewById(R.id.btnApplicants)
        val btnEdit: Button = v.findViewById(R.id.btnEdit)
    }
}