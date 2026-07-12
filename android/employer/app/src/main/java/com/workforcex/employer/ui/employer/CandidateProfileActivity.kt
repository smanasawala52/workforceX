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
                        binding.tvName.text = p.name
                        binding.tvMobile.text = "Mobile: ${p.mobileNumber}"
                        binding.tvEmail.text = "Email: ${p.email}"
                        binding.tvLocation.text = "Location: ${p.city}, ${p.state}"
                        binding.tvSkills.text = "Skills: ${p.skills}"
                        binding.tvExperience.text = "Experience: ${p.experience} years"
                        binding.tvSalary.text = "Expected Salary: ₹${p.preferredSalary.toInt()}/month"
                        binding.tvAvailability.text = "Availability: ${p.availability}"
                        binding.tvLanguages.text = "Languages: ${p.languages}"
                        binding.tvDescription.text = "Description: ${p.description}"
                    } else {
                        Toast.makeText(this@CandidateProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<WorkerProfileResponse>, t: Throwable) {
                    Toast.makeText(this@CandidateProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }
}