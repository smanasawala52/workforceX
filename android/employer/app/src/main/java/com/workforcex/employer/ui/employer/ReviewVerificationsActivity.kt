package com.workforcex.employer.ui.employer

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.workforcex.employer.databinding.ActivityReviewVerificationsBinding
import com.workforcex.employer.utils.TokenManager
import com.workforcex.shared_employer.RetrofitClient
import com.workforcex.shared_employer.models.Verification
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewVerificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewVerificationsBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewVerificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenManager = TokenManager(this)

        title = "Review Verifications"
        binding.rvVerifications.layoutManager = LinearLayoutManager(this)
        loadPendingVerifications()
    }

    private fun loadPendingVerifications() {
        binding.progressBar.visibility = View.VISIBLE
        RetrofitClient.get().getPendingVerifications(tokenManager.getBearerToken())
            .enqueue(object : Callback<List<Verification>> {
                override fun onResponse(
                    call: Call<List<Verification>>,
                    response: Response<List<Verification>>
                ) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        binding.rvVerifications.adapter =
                            VerificationAdapter(response.body()!!, this@ReviewVerificationsActivity::updateVerificationStatus)
                    } else {
                        Toast.makeText(this@ReviewVerificationsActivity, "Failed to load verifications", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Verification>>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@ReviewVerificationsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateVerificationStatus(verificationId: String, status: String, comments: String) {
        RetrofitClient.get().updateEmployerVerificationStatus(tokenManager.getBearerToken(), verificationId, status, comments)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ReviewVerificationsActivity, "Verification status updated", Toast.LENGTH_SHORT).show()
                        loadPendingVerifications() // Refresh the list
                    } else {
                        Toast.makeText(this@ReviewVerificationsActivity, "Failed to update status", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@ReviewVerificationsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
