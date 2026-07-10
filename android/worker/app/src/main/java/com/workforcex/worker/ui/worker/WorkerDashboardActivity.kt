package com.workforcex.worker.ui.worker

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.workforcex.worker.R
import com.workforcex.shared.models.Notification
import com.workforcex.shared.RetrofitClient
import com.workforcex.shared.models.WorkerProfileResponse
import com.workforcex.worker.databinding.ActivityWorkerDashboardBinding
import com.workforcex.worker.ui.auth.LoginActivity
import com.workforcex.worker.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WorkerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkerDashboardBinding
    private lateinit var tokenManager: TokenManager
    private var notificationBadge: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenManager = TokenManager(this)

        setSupportActionBar(binding.toolbar)
        title = "Worker Dashboard"

        binding.btnBrowseJobs.setOnClickListener { startActivity(Intent(this, BrowseJobsActivity::class.java)) }
        binding.btnMyApplications.setOnClickListener { startActivity(Intent(this, MyApplicationsActivity::class.java)) }
        binding.btnEditProfile.setOnClickListener { startActivity(Intent(this, WorkerProfileActivity::class.java)) }
        binding.btnVerification.setOnClickListener { startActivity(Intent(this, VerificationActivity::class.java)) }
        binding.btnLogout.setOnClickListener {
            tokenManager.clear()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
        loadNotificationCount()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        val menuItem = menu.findItem(R.id.action_notifications)
        val actionView = menuItem.actionView
        notificationBadge = actionView?.findViewById(R.id.notification_badge)
        actionView?.setOnClickListener { onOptionsItemSelected(menuItem) }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_notifications) {
            startActivity(Intent(this, NotificationsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadProfile() {
        RetrofitClient.get().getWorkerProfile(tokenManager.getBearerToken())
            .enqueue(object : Callback<WorkerProfileResponse> {
                override fun onResponse(
                    call: Call<WorkerProfileResponse>,
                    response: Response<WorkerProfileResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val p = response.body()!!
                        binding.tvWelcome.text = "Welcome, " + (p.name ?: tokenManager.getMobile())
                        binding.tvSkills.text = "Skills: " + (p.skills ?: "Not set")
                        binding.tvExperience.text = "Experience: " + (p.experience?.let { "$it yrs" } ?: "Not set")
                    }
                }

                override fun onFailure(call: Call<WorkerProfileResponse>, t: Throwable) {}
            })
    }

    private fun loadNotificationCount() {
        RetrofitClient.get().getUnreadNotifications(tokenManager.getBearerToken())
            .enqueue(object : Callback<List<Notification>> {
                override fun onResponse(
                    call: Call<List<Notification>>,
                    response: Response<List<Notification>>
                ) {
                    if (response.isSuccessful && response.body() != null && notificationBadge != null) {
                        val count = response.body()!!.size
                        if (count > 0) {
                            notificationBadge!!.text = count.toString()
                            notificationBadge!!.visibility = View.VISIBLE
                        } else {
                            notificationBadge!!.visibility = View.GONE
                        }
                    }
                }

                override fun onFailure(call: Call<List<Notification>>, t: Throwable) {}
            })
    }
}