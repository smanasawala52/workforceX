package com.workforcex.employer.ui.employer

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.workforcex.employer.databinding.ActivityVerifyWorkerBinding
import com.workforcex.employer.utils.TokenManager
import com.workforcex.shared_employer.RetrofitClient
import com.workforcex.shared_employer.models.Verification
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerifyWorkerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyWorkerBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var workerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyWorkerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenManager = TokenManager(this)

        workerId = intent.getStringExtra("workerId") ?: ""
        val workerName = intent.getStringExtra("workerName")
        title = "Verify $workerName"

        binding.rvDocuments.layoutManager = LinearLayoutManager(this)
        loadWorkerDocuments()
    }

    private fun loadWorkerDocuments() {
        binding.progressBar.visibility = View.VISIBLE
        // This endpoint doesn't exist yet, so we'll need to add it
        RetrofitClient.get().getWorkerDocuments(tokenManager.getBearerToken(), workerId)
            .enqueue(object : Callback<List<Verification>> {
                override fun onResponse(
                    call: Call<List<Verification>>,
                    response: Response<List<Verification>>
                ) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        binding.rvDocuments.adapter =
                            DocumentAdapter(response.body()!!, this@VerifyWorkerActivity::updateVerificationStatus)
                    } else {
                        Toast.makeText(this@VerifyWorkerActivity, "Failed to load documents", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Verification>>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@VerifyWorkerActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateVerificationStatus(verificationId: String, status: String, comments: String) {
        RetrofitClient.get().updateEmployerVerificationStatus(tokenManager.getBearerToken(), verificationId, status, comments)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@VerifyWorkerActivity, "Verification status updated", Toast.LENGTH_SHORT).show()
                        loadWorkerDocuments() // Refresh the list
                    } else {
                        Toast.makeText(this@VerifyWorkerActivity, "Failed to update status", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@VerifyWorkerActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
