package com.workforcex.worker.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.workforcex.shared.Result
import com.workforcex.worker.databinding.ActivityLoginBinding
import com.workforcex.worker.ui.worker.WorkerDashboardActivity
import com.workforcex.worker.utils.TokenManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenManager = TokenManager(this)
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginViewModel.loginResult.observe(this, Observer { result ->
            setLoading(result is Result.Loading)
            when (result) {
                is Result.Success -> {
                    val body = result.data
                    if ("WORKER" != body.role) {
                        Toast.makeText(this, "Please use the Employer app to login as an Employer", Toast.LENGTH_LONG).show()
                        return@Observer
                    }
                    tokenManager.save(body.token, body.role, body.mobileNumber)
                    val intent = Intent(this, WorkerDashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                is Result.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        })
    }

    private fun attemptLogin() {
        val mobile = binding.etMobile.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (mobile.length != 10 || !mobile.all { it.isDigit() }) {
            binding.etMobile.error = "Enter a valid 10-digit mobile number"
            return
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            return
        }

        val selectedCountryCode = binding.spinnerCountryCode.selectedItem.toString().split(" ")[0]
        loginViewModel.login(mobile, password, selectedCountryCode)
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
    }
}
