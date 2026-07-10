package com.workforcex.employer.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.workforcex.employer.ui.employer.EmployerDashboardActivity
import com.workforcex.employer.utils.TokenManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tokenManager = TokenManager(this)
        if (tokenManager.isLoggedIn) {
            startActivity(Intent(this, EmployerDashboardActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}