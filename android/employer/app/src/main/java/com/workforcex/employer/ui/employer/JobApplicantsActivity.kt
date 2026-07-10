package com.workforcex.employer.ui.employer

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.workforcex.employer.R
import com.workforcex.shared_employer.models.JobApplicationItem
import com.workforcex.shared_employer.RetrofitClient
import com.workforcex.employer.databinding.ActivityJobApplicantsBinding
import com.workforcex.employer.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JobApplicantsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobApplicantsBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var jobId: String
    private lateinit var adapter: ApplicantAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobApplicantsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenManager = TokenManager(this)

        jobId = intent.getStringExtra("jobId")!!
        val jobTitle = intent.getStringExtra("jobTitle")
        title = "Applicants"
        binding.tvTitle.text = "Applicants for: $jobTitle"
        binding.rvApplicants.layoutManager = LinearLayoutManager(this)
        loadApplicants()
    }

    private fun loadApplicants() {
        binding.progressBar.visibility = View.VISIBLE
        RetrofitClient.get().getApplicationsForJob(tokenManager.getBearerToken(), jobId)
            .enqueue(object : Callback<List<JobApplicationItem>> {
                override fun onResponse(
                    call: Call<List<JobApplicationItem>>,
                    response: Response<List<JobApplicationItem>>
                ) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        val items = response.body()!!
                        binding.tvCount.text = items.size.toString() + " applicant(s)"
                        binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                        adapter = ApplicantAdapter(items, this@JobApplicantsActivity::updateStatus)
                        binding.rvApplicants.adapter = adapter
                    }
                }

                override fun onFailure(call: Call<List<JobApplicationItem>>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@JobApplicantsActivity,
                        "Network error", Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updateStatus(item: JobApplicationItem, status: String, position: Int) {
        RetrofitClient.get().updateApplicationStatus(
            tokenManager.getBearerToken(), item.applicationId, status
        )
            .enqueue(object : Callback<JobApplicationItem> {
                override fun onResponse(
                    call: Call<JobApplicationItem>,
                    response: Response<JobApplicationItem>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        item.status = response.body()!!.status
                        adapter.notifyItemChanged(position)
                        Toast.makeText(
                            this@JobApplicantsActivity,
                            (item.workerName ?: "Applicant") + " " + status.lowercase() + "d", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JobApplicationItem>, t: Throwable) {
                    Toast.makeText(
                        this@JobApplicantsActivity,
                        "Network error", Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    internal class ApplicantAdapter(
        private val items: List<JobApplicationItem>,
        private val onAction: (JobApplicationItem, String, Int) -> Unit
    ) : RecyclerView.Adapter<ApplicantAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_applicant, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, pos: Int) {
            val item = items[pos]

            h.tvName.text = item.workerName ?: item.workerMobile
            h.tvMobile.text = "📞 " + (item.workerMobile ?: "")
            h.tvSkills.text = "Skills: " + (item.workerSkills ?: "—")

            var details = ""
            if (item.workerExperience != null) details += item.workerExperience.toString() + " yrs"
            if (item.workerCity != null) details += (if (details.isEmpty()) "" else " · ") + item.workerCity
            if (item.workerPreferredSalary != null)
                details += "  ₹" + item.workerPreferredSalary.toInt()
            h.tvDetails.text = details

            val status = item.status ?: "PENDING"
            h.tvStatus.text = status
            when (status) {
                "SHORTLISTED" -> h.tvStatus.setTextColor(Color.parseColor("#1B5E20"))
                "REJECTED" -> h.tvStatus.setTextColor(Color.parseColor("#B71C1C"))
                "INTERVIEW" -> h.tvStatus.setTextColor(Color.parseColor("#FFC107"))
                "OFFERED" -> h.tvStatus.setTextColor(Color.parseColor("#FF9800"))
                "HIRED" -> h.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                else -> h.tvStatus.setTextColor(Color.parseColor("#1565C0"))
            }

            val actionable = "REJECTED" != status && "HIRED" != status
            h.btnShortlist.isEnabled = "PENDING" == status
            h.btnInterview.isEnabled = "SHORTLISTED" == status
            h.btnOffer.isEnabled = "INTERVIEW" == status
            h.btnHire.isEnabled = "OFFERED" == status
            h.btnReject.isEnabled = actionable

            h.btnShortlist.setOnClickListener {
                onAction(item, "SHORTLISTED", h.adapterPosition)
            }
            h.btnReject.setOnClickListener {
                onAction(item, "REJECTED", h.adapterPosition)
            }
            h.btnInterview.setOnClickListener {
                onAction(item, "INTERVIEW", h.adapterPosition)
            }
            h.btnOffer.setOnClickListener {
                onAction(item, "OFFERED", h.adapterPosition)
            }
            h.btnHire.setOnClickListener {
                onAction(item, "HIRED", h.adapterPosition)
            }
        }

        override fun getItemCount(): Int = items.size

        internal class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvName: TextView = v.findViewById(R.id.tvName)
            val tvMobile: TextView = v.findViewById(R.id.tvMobile)
            val tvSkills: TextView = v.findViewById(R.id.tvSkills)
            val tvDetails: TextView = v.findViewById(R.id.tvDetails)
            val tvStatus: TextView = v.findViewById(R.id.tvStatus)
            val btnShortlist: Button = v.findViewById(R.id.btnShortlist)
            val btnReject: Button = v.findViewById(R.id.btnReject)
            val btnInterview: Button = v.findViewById(R.id.btnInterview)
            val btnOffer: Button = v.findViewById(R.id.btnOffer)
            val btnHire: Button = v.findViewById(R.id.btnHire)
        }
    }
}