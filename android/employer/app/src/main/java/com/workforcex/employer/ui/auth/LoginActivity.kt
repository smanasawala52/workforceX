package com.workforcex.employer.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.workforcex.shared_employer.models.LoginRequest
import com.workforcex.shared_employer.models.LoginResponse
import com.workforcex.shared_employer.RetrofitClient
import com.workforcex.employer.databinding.ActivityLoginBinding
import com.workforcex.employer.ui.employer.EmployerDashboardActivity
import com.workforcex.employer.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenManager = TokenManager(this)
        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val mobile = binding.etMobile.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        if (mobile.length != 10) {
            binding.etMobile.error = "Enter a valid 10-digit mobile number"
            return
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            return
        }
        setLoading(true)

        val selectedCountryCode = binding.spinnerCountryCode.selectedItem.toString().split(" ")[0]

        RetrofitClient.get().login(LoginRequest(mobile, password, selectedCountryCode))
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    setLoading(false)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if ("EMPLOYER" != body.role) {
                            Toast.makeText(
                                this@LoginActivity,
                                "Please use the Worker app to login as a Worker",
                                Toast.LENGTH_LONG
                            ).show()
                            return
                        }
                        tokenManager.save(body.token, body.role, body.mobileNumber)
                        val intent = Intent(this@LoginActivity, EmployerDashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid mobile number or password", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    setLoading(false)
                    Toast.makeText(this@LoginActivity, "Network error: " + t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
    }
}
