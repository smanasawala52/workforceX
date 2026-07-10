package com.workforcex.worker.ui.worker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.workforcex.shared.models.JobApplication
import com.workforcex.shared.models.JobBrowseItem
import com.workforcex.shared.RetrofitClient
import com.workforcex.worker.databinding.ActivityJobDetailsBinding
import com.workforcex.worker.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JobDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobDetailsBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var job: JobBrowseItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenManager = TokenManager(this)

        job = intent.getSerializableExtra("job") as JobBrowseItem
        val isApplied = intent.getBooleanExtra("isApplied", false)

        title = "Job Details"
        populateJobDetails()

        binding.btnApply.isEnabled = !isApplied
        binding.btnApply.text = if (isApplied) "Already Applied" else "Apply Now"
        binding.btnApply.setOnClickListener {
            if (!isApplied) {
                applyToJob()
            }
        }
    }

    private fun populateJobDetails() {
        binding.tvJobTitle.text = job.title
        binding.tvCompanyName.text = job.companyName
        binding.tvLocation.text = "📍 " + job.location
        binding.tvSkills.text = "Skills: " + job.skillsRequired
        binding.tvExperience.text = "Experience: " + job.experienceRequired + " years"
        binding.tvDescription.text = job.description

        if (job.salaryMin != null && job.salaryMax != null) {
            binding.tvSalary.text = "₹" + job.salaryMin.toInt() + " – ₹" + job.salaryMax.toInt() + "/month"
        } else if (job.salaryMin != null) {
            binding.tvSalary.text = "₹" + job.salaryMin.toInt() + "+/month"
        } else {
            binding.tvSalary.text = "Salary not disclosed"
        }
    }

    private fun applyToJob() {
        binding.btnApply.isEnabled = false
        RetrofitClient.get().applyToJob(tokenManager.getBearerToken(), job.id)
            .enqueue(object : Callback<JobApplication> {
                override fun onResponse(call: Call<JobApplication>, response: Response<JobApplication>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@JobDetailsActivity, "Applied successfully!", Toast.LENGTH_SHORT).show()
                        binding.btnApply.text = "Applied"
                    } else {
                        Toast.makeText(this@JobDetailsActivity, "Already applied or error.", Toast.LENGTH_SHORT).show()
                        binding.btnApply.isEnabled = true
                    }
                }

                override fun onFailure(call: Call<JobApplication>, t: Throwable) {
                    Toast.makeText(this@JobDetailsActivity, "Network error.", Toast.LENGTH_SHORT).show()
                    binding.btnApply.isEnabled = true
                }
            })
    }
}