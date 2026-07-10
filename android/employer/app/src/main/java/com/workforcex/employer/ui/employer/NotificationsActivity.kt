package com.workforcex.employer.ui.employer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.workforcex.shared_employer.models.Notification
import com.workforcex.shared_employer.RetrofitClient
import com.workforcex.employer.databinding.ActivityNotificationsBinding
import com.workforcex.employer.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenManager = TokenManager(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Notifications"

        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        loadNotifications()
    }

    private fun loadNotifications() {
        RetrofitClient.get().getUnreadNotifications(tokenManager.getBearerToken())
            .enqueue(object : Callback<List<Notification>> {
                override fun onResponse(
                    call: Call<List<Notification>>,
                    response: Response<List<Notification>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        binding.rvNotifications.adapter =
                            NotificationAdapter(response.body()!!, this@NotificationsActivity::onNotificationClicked)
                    }
                }

                override fun onFailure(call: Call<List<Notification>>, t: Throwable) {
                    Toast.makeText(this@NotificationsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun onNotificationClicked(notification: Notification) {
        if ("JOB_APPLICANTS" == notification.linkType && notification.linkId != null) {
            val intent = Intent(this, JobApplicantsActivity::class.java)
            intent.putExtra("jobId", notification.linkId)
            // We don't have the job title here, so the title will be generic
            intent.putExtra("jobTitle", notification.jobTitle)
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    internal class NotificationAdapter(
        private val items: List<Notification>,
        private val onClick: (Notification) -> Unit
    ) : RecyclerView.Adapter<NotificationAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, pos: Int) {
            val item = items[pos]
            h.text.text = item.message
            h.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount(): Int = items.size

        internal class VH(v: View) : RecyclerView.ViewHolder(v) {
            var text: TextView = v.findViewById(android.R.id.text1)
        }
    }
}