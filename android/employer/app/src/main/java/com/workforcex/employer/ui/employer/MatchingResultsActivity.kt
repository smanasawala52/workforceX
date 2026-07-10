package com.workforcex.employer.ui.employer

import android.content.Intent
import android.net.Uri
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
import com.workforcex.shared_employer.models.MatchedWorker
import com.workforcex.shared_employer.RetrofitClient
import com.workforcex.employer.databinding.ActivityMatchingResultsBinding
import com.workforcex.employer.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder

class MatchingResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMatchingResultsBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var jobId: String
    private lateinit var jobTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchingResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenManager = TokenManager(this)

        this.jobId = intent.getStringExtra("jobId")!!
        this.jobTitle = intent.getStringExtra("jobTitle")!!

        title = "Candidates: $jobTitle"
        binding.tvJobTitle.text = "Ranked candidates for: $jobTitle"

        binding.rvCandidates.layoutManager = LinearLayoutManager(this)

        loadCandidates(this.jobId)
    }

    private fun loadCandidates(jobId: String) {
        binding.progressBar.visibility = View.VISIBLE
        RetrofitClient.get().getMatchedWorkers(tokenManager.getBearerToken(), jobId)
            .enqueue(object : Callback<List<MatchedWorker>> {
                override fun onResponse(
                    call: Call<List<MatchedWorker>>,
                    response: Response<List<MatchedWorker>>
                ) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        val workers = response.body()!!
                        binding.tvEmpty.visibility = if (workers.isEmpty()) View.VISIBLE else View.GONE
                        binding.rvCandidates.adapter =
                            CandidateAdapter(workers, this@MatchingResultsActivity::offerJob, jobTitle)
                    } else {
                        Toast.makeText(
                            this@MatchingResultsActivity,
                            "Failed to load candidates",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<MatchedWorker>>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@MatchingResultsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun offerJob(worker: MatchedWorker, button: Button) {
        button.isEnabled = false
        button.text = "Offering..."

        RetrofitClient.get().offerJob(tokenManager.getBearerToken(), this.jobId, worker.workerId)
            .enqueue(object : Callback<JobApplicationItem> {
                override fun onResponse(
                    call: Call<JobApplicationItem>,
                    response: Response<JobApplicationItem>
                ) {
                    if (response.isSuccessful) {
                        button.text = "Offered"
                        Toast.makeText(
                            this@MatchingResultsActivity,
                            "Offer sent to " + worker.name,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        button.isEnabled = true
                        button.text = "Offer Job"
                        Toast.makeText(
                            this@MatchingResultsActivity,
                            "Failed to send offer. The worker may have already applied.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JobApplicationItem>, t: Throwable) {
                    button.isEnabled = true
                    button.text = "Offer Job"
                    Toast.makeText(this@MatchingResultsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    internal class CandidateAdapter(
        private val items: List<MatchedWorker>,
        private val onOfferClick: (MatchedWorker, Button) -> Unit,
        private val jobTitle: String
    ) : RecyclerView.Adapter<CandidateAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_candidate, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, pos: Int) {
            val item = items[pos]
            h.tvRank.text = (pos + 1).toString()
            h.tvName.text = item.name
            h.tvScore.text = String.format("%.0f%% Match", item.score)
            h.tvSkills.text = "Skills: " + item.skills
            h.tvExperience.text = "Experience: " + item.experience + " yrs"
            h.tvCity.text = "City: " + item.city
            h.tvSalary.text = "Prefers: ₹" + item.preferredSalary.toInt()
            h.tvMobile.text = "📞 " + item.mobileNumber

            h.btnOfferJob.setOnClickListener { v -> onOfferClick(item, h.btnOfferJob) }

            h.btnCall.setOnClickListener { v ->
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:" + item.mobileNumber)
                v.context.startActivity(intent)
            }

            h.btnWhatsApp.setOnClickListener { v ->
                try {
                    val message = URLEncoder.encode(
                        "Hello " + item.name + ", we have a job offer for you for the position of '" + this.jobTitle + "'. Are you interested?",
                        "UTF-8"
                    )
                    val url = "https://wa.me/+91" + item.mobileNumber + "?text=" + message
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    v.context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(v.context, "WhatsApp not installed or error.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        override fun getItemCount(): Int = items.size

        internal class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvRank: TextView = v.findViewById(R.id.tvRank)
            val tvName: TextView = v.findViewById(R.id.tvName)
            val tvScore: TextView = v.findViewById(R.id.tvScore)
            val tvSkills: TextView = v.findViewById(R.id.tvSkills)
            val tvExperience: TextView = v.findViewById(R.id.tvExperience)
            val tvCity: TextView = v.findViewById(R.id.tvCity)
            val tvSalary: TextView = v.findViewById(R.id.tvSalary)
            val tvMobile: TextView = v.findViewById(R.id.tvMobile)
            val btnOfferJob: Button = v.findViewById(R.id.btnOfferJob)
            val btnCall: Button = v.findViewById(R.id.btnCall)
            val btnWhatsApp: Button = v.findViewById(R.id.btnWhatsApp)
        }
    }
}