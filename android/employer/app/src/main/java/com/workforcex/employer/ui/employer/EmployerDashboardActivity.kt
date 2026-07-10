package com.workforcex.employer.ui.employer

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.workforcex.employer.R
import com.workforcex.shared_employer.models.EmployerProfileResponse
import com.workforcex.shared_employer.models.Notification
import com.workforcex.shared_employer.RetrofitClient
import com.workforcex.employer.databinding.ActivityEmployerDashboardBinding
import com.workforcex.employer.ui.auth.LoginActivity
import com.workforcex.employer.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmployerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmployerDashboardBinding
    private lateinit var tokenManager: TokenManager
    private var badge: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenManager = TokenManager(this)

        setSupportActionBar(binding.toolbar)

        binding.btnMyJobs.setOnClickListener {
            startActivity(Intent(this, JobsActivity::class.java))
        }

        binding.btnSearchCandidates.setOnClickListener {
            startActivity(Intent(this, SearchCandidatesActivity::class.java))
        }

        binding.btnLogout.setOnClickListener { logout() }

        loadProfile()
    }

    override fun onResume() {
        super.onResume()
        // Refresh notification count when returning to the screen
        if (badge != null) {
            loadNotificationCount()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        val menuItem = menu.findItem(R.id.action_notifications)
        val actionView = menuItem.actionView
        badge = actionView?.findViewById(R.id.badge)

        actionView?.setOnClickListener { onOptionsItemSelected(menuItem) }

        loadNotificationCount()
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
        RetrofitClient.get().getEmployerProfile(tokenManager.getBearerToken())
            .enqueue(object : Callback<EmployerProfileResponse> {
                override fun onResponse(
                    call: Call<EmployerProfileResponse>,
                    response: Response<EmployerProfileResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val p = response.body()!!
                        val company = p.companyName ?: tokenManager.getMobile()
                        binding.tvWelcome.text = "Welcome, $company"
                    } else {
                        binding.tvWelcome.text = "Welcome, " + tokenManager.getMobile()
                    }
                }

                override fun onFailure(call: Call<EmployerProfileResponse>, t: Throwable) {
                    binding.tvWelcome.text = "Welcome, " + tokenManager.getMobile()
                }
            })
    }

    private fun loadNotificationCount() {
        RetrofitClient.get().getUnreadNotifications(tokenManager.getBearerToken())
            .enqueue(object : Callback<List<Notification>> {
                override fun onResponse(
                    call: Call<List<Notification>>,
                    response: Response<List<Notification>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val count = response.body()!!.size
                        if (badge != null) {
                            if (count > 0) {
                                badge!!.text = count.toString()
                                badge!!.visibility = View.VISIBLE
                            } else {
                                badge!!.visibility = View.GONE
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<List<Notification>>, t: Throwable) {}
            })
    }

    private fun logout() {
        tokenManager.clear()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}