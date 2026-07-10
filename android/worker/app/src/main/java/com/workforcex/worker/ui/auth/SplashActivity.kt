package com.workforcex.worker.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.workforcex.worker.utils.TokenManager
import com.workforcex.worker.ui.worker.WorkerDashboardActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tokenManager = TokenManager(this)
        if (tokenManager.isLoggedIn) {
            startActivity(Intent(this, WorkerDashboardActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}