package com.workforcex.worker.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.workforcex.worker.utils.TokenManager
import com.workforcex.worker.ui.worker.WorkerDashboardActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SplashActivity", "onCreate: Start")
        try {
            val tokenManager = TokenManager(this)
            if (tokenManager.isLoggedIn) {
                Log.d("SplashActivity", "User is logged in, starting WorkerDashboardActivity")
                startActivity(Intent(this, WorkerDashboardActivity::class.java))
            } else {
                Log.d("SplashActivity", "User is not logged in, starting LoginActivity")
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error in onCreate", e)
        }
        Log.d("SplashActivity", "onCreate: End")
    }
}