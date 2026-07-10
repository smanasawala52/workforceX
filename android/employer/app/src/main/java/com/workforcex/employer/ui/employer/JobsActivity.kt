package com.workforcex.employer.ui.employer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.workforcex.shared_employer.models.JobResponse
import com.workforcex.shared_employer.RetrofitClient
import com.workforcex.employer.databinding.ActivityJobsBinding
import com.workforcex.employer.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JobsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobsBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var adapter: JobAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "My Jobs"
        tokenManager = TokenManager(this)

        adapter = JobAdapter(
            ArrayList(),
            this::onFindCandidates,
            this::onViewApplicants,
            this::onEditJob
        )
        binding.rvJobs.layoutManager = LinearLayoutManager(this)
        binding.rvJobs.adapter = adapter

        binding.btnCreateJob.setOnClickListener {
            startActivity(Intent(this, JobCreateEditActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadJobs()
    }

    private fun loadJobs() {
        binding.progressBar.visibility = View.VISIBLE
        RetrofitClient.get().getMyJobs(tokenManager.getBearerToken())
            .enqueue(object : Callback<List<JobResponse>> {
                override fun onResponse(
                    call: Call<List<JobResponse>>,
                    response: Response<List<JobResponse>>
                ) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        val jobs = response.body()!!
                        adapter.updateJobs(jobs)
                        binding.tvEmpty.visibility =
                            if (jobs.isEmpty()) View.VISIBLE else View.GONE
                    }
                }

                override fun onFailure(call: Call<List<JobResponse>>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@JobsActivity,
                        "Failed to load jobs", Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun onFindCandidates(job: JobResponse) {
        val intent = Intent(this, MatchingResultsActivity::class.java)
        intent.putExtra("jobId", job.id)
        intent.putExtra("jobTitle", job.title)
        startActivity(intent)
    }

    private fun onViewApplicants(job: JobResponse) {
        val intent = Intent(this, JobApplicantsActivity::class.java)
        intent.putExtra("jobId", job.id)
        intent.putExtra("jobTitle", job.title)
        startActivity(intent)
    }

    private fun onEditJob(job: JobResponse) {
        val intent = Intent(this, JobCreateEditActivity::class.java)
        intent.putExtra("jobId", job.id)
        intent.putExtra("jobTitle", job.title)
        intent.putExtra("jobSkills", job.skillsRequired)
        intent.putExtra("jobExperience", job.experienceRequired ?: 0)
        intent.putExtra("jobLocation", job.location)
        intent.putExtra("jobSalaryMin", job.salaryMin ?: 0.0)
        intent.putExtra("jobSalaryMax", job.salaryMax ?: 0.0)
        intent.putExtra("jobOpenPositions", job.openPositions ?: 0)
        intent.putExtra("jobDescription", job.description)
        startActivity(intent)
    }
}