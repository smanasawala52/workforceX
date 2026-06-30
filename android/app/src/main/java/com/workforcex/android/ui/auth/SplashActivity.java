package com.workforcex.android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.workforcex.android.utils.TokenManager;
import com.workforcex.android.ui.worker.WorkerDashboardActivity;
import com.workforcex.android.ui.employer.EmployerDashboardActivity;

/**
 * Entry point. Decides where to send the user based on their stored session.
 * No layout needed — this screen is never visible.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TokenManager tokenManager = new TokenManager(this);

        if (!tokenManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
        } else if ("WORKER".equals(tokenManager.getRole())) {
            startActivity(new Intent(this, WorkerDashboardActivity.class));
        } else {
            startActivity(new Intent(this, EmployerDashboardActivity.class));
        }

        finish(); // remove SplashActivity from back stack
    }
}
