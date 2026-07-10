package com.workforcex.worker.ui.worker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.workforcex.worker.R
import com.workforcex.shared.models.Notification
import com.workforcex.shared.RetrofitClient
import com.workforcex.worker.databinding.ActivityNotificationsBinding
import com.workforcex.worker.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Notifications"
        tokenManager = TokenManager(this)
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        loadNotifications()
    }

    private fun loadNotifications() {
        binding.tvEmpty.visibility = View.GONE
        RetrofitClient.get().getUnreadNotifications(tokenManager.getBearerToken())
            .enqueue(object : Callback<List<Notification>> {
                override fun onResponse(
                    call: Call<List<Notification>>,
                    response: Response<List<Notification>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val notifications = response.body()!!
                        Collections.sort(notifications, compareByDescending { it.createdAt })
                        binding.rvNotifications.adapter =
                            NotificationAdapter(notifications, this@NotificationsActivity::onNotificationClicked)
                    }
                }

                override fun onFailure(call: Call<List<Notification>>, t: Throwable) {
                    Toast.makeText(this@NotificationsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun onNotificationClicked(notification: Notification) {
        if ("MY_APPLICATIONS" == notification.linkType) {
            val intent = Intent(this, MyApplicationsActivity::class.java)
            startActivity(intent)
        }
    }

    internal class NotificationAdapter(
        private val items: List<Notification>,
        private val onClick: (Notification) -> Unit
    ) : RecyclerView.Adapter<NotificationAdapter.VH>() {
        private val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        private val outputFormatter = SimpleDateFormat("dd-MMM-yyyy, hh:mm a", Locale.ENGLISH)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notification, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, pos: Int) {
            val item = items[pos]
            h.tvMessage.text = item.message

            try {
                val localDateTime = LocalDateTime.parse(item.createdAt, inputFormatter)
                val date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
                h.tvDate.text = outputFormatter.format(date)
            } catch (e: Exception) {
                h.tvDate.text = if (item.createdAt != null) item.createdAt.substring(0, 10) else ""
            }

            if (pos % 2 == 0) {
                h.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"))
            } else {
                h.itemView.setBackgroundColor(Color.parseColor("#E3F2FD"))
            }

            h.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount(): Int = items.size

        internal class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvMessage: TextView = v.findViewById(R.id.tvMessage)
            val tvDate: TextView = v.findViewById(R.id.tvDate)
        }
    }
}