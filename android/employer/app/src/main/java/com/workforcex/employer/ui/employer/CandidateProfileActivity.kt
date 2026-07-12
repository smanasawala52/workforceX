package com.workforcex.employer.ui.employer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.workforcex.employer.databinding.ActivityCandidateProfileBinding
import com.workforcex.shared_employer.RetrofitClient
import com.workforcex.shared_employer.models.WorkerProfileResponse
import com.workforcex.employer.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CandidateProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCandidateProfileBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCandidateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Candidate Profile"
        tokenManager = TokenManager(this)

        val workerId = intent.getStringExtra("workerId")
        if (workerId == null) {
            Toast.makeText(this, "Worker ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadWorkerProfile(workerId)
    }

    private fun loadWorkerProfile(workerId: String) {
        RetrofitClient.get().getWorkerProfile(tokenManager.getBearerToken(), workerId)
            .enqueue(object : Callback<WorkerProfileResponse> {
                override fun onResponse(
                    call: Call<WorkerProfileResponse>,
                    response: Response<WorkerProfileResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val p = response.body()!!

                        showOrHide(binding.tvName, p.name) { it }
                        showOrHide(binding.tvMobile, p.mobileNumber) { "Mobile: $it" }
                        showOrHide(binding.tvEmail, p.email) { "Email: $it" }

                        val location = listOfNotNull(
                            p.city?.takeIf { it.isNotEmpty() },
                            p.state?.takeIf { it.isNotEmpty() }
                        ).joinToString(", ")
                        if (location.isEmpty()) {
                            binding.tvLocation.visibility = android.view.View.GONE
                        } else {
                            binding.tvLocation.visibility = android.view.View.VISIBLE
                            binding.tvLocation.text = "Location: $location"
                        }

                        showOrHide(binding.tvSkills, p.skills) { "Skills: $it" }

                        if (p.experience == null) {
                            binding.tvExperience.visibility = android.view.View.GONE
                        } else {
                            binding.tvExperience.visibility = android.view.View.VISIBLE
                            binding.tvExperience.text = "Experience: ${p.experience} years"
                        }

                        if (p.preferredSalary == null) {
                            binding.tvSalary.visibility = android.view.View.GONE
                        } else {
                            binding.tvSalary.visibility = android.view.View.VISIBLE
                            binding.tvSalary.text = "Expected Salary: ₹${p.preferredSalary.toInt()}/month"
                        }

                        showOrHide(binding.tvAvailability, p.availability) { "Availability: $it" }
                        showOrHide(binding.tvLanguages, p.languages) { "Languages: $it" }
                        showOrHide(binding.tvDescription, p.description) { "Description: $it" }
                    } else {
                        Toast.makeText(this@CandidateProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<WorkerProfileResponse>, t: Throwable) {
                    Toast.makeText(this@CandidateProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    /**
     * Hides [view] when [value] is null or empty; otherwise shows it and sets its
     * text to the result of [format] applied to the value.
     */
    private fun showOrHide(view: android.widget.TextView, value: String?, format: (String) -> String) {
        if (value.isNullOrEmpty()) {
            view.visibility = android.view.View.GONE
        } else {
            view.visibility = android.view.View.VISIBLE
            view.text = format(value)
        }
    }
}