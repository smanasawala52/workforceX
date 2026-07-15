package com.workforcex.employer.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.workforcex.shared_employer.models.RegisterRequest
import com.workforcex.shared_employer.models.RegisterResponse
import com.workforcex.shared_employer.RetrofitClient
import com.workforcex.employer.databinding.ActivityRegisterBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnRegister.setOnClickListener { attemptRegister() }
        binding.tvGoToLogin.setOnClickListener { finish() }
    }

    private fun attemptRegister() {
        val mobile = binding.etMobile.text.toString().trim()
        if (mobile.length != 10) {
            binding.etMobile.error = "Enter a valid 10-digit mobile number"
            return
        }
        setLoading(true)

        val selectedCountryCode = binding.spinnerCountryCode.selectedItem.toString().split(" ")[0]

        // Employer app always registers as EMPLOYER — no role selector needed
        RetrofitClient.get().register(RegisterRequest(mobile, "EMPLOYER", selectedCountryCode))
            .enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    setLoading(false)
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Account created! Please login.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Mobile number already registered", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    setLoading(false)
                    Toast.makeText(this@RegisterActivity, "Network error: " + t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !loading
    }
}
