package com.workforcex.worker.ui.worker

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.workforcex.worker.R
import com.workforcex.shared.models.JobApplication
import com.workforcex.shared.RetrofitClient
import com.workforcex.worker.databinding.ActivityMyApplicationsBinding
import com.workforcex.worker.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder

class MyApplicationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyApplicationsBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyApplicationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "My Applications"
        tokenManager = TokenManager(this)
        binding.rvApplications.layoutManager = LinearLayoutManager(this)
        loadApplications()
    }

    private fun loadApplications() {
        binding.progressBar.visibility = View.VISIBLE
        RetrofitClient.get().getMyApplications(tokenManager.getBearerToken())
            .enqueue(object : Callback<List<JobApplication>> {
                override fun onResponse(
                    call: Call<List<JobApplication>>,
                    response: Response<List<JobApplication>>
                ) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        val apps = response.body()!!
                        binding.tvEmpty.visibility = if (apps.isEmpty()) View.VISIBLE else View.GONE
                        binding.rvApplications.adapter = ApplicationsAdapter(apps)
                    }
                }

                override fun onFailure(call: Call<List<JobApplication>>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@MyApplicationsActivity,
                        "Network error: " + t.message, Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    internal class ApplicationsAdapter(private val items: List<JobApplication>) :
        RecyclerView.Adapter<ApplicationsAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_application, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, pos: Int) {
            val app = items[pos]
            h.tvJobTitle.text = app.jobTitle ?: "Job"
            h.tvCompany.text = app.companyName ?: ""
            h.tvAppliedAt.text = if (app.appliedAt != null) "Applied: " + app.appliedAt.substring(0, 10) else ""
            h.tvStatus.text = app.status ?: "PENDING"
            when (app.status ?: "PENDING") {
                "SHORTLISTED" -> h.tvStatus.setTextColor(Color.parseColor("#1B5E20"))
                "REJECTED" -> h.tvStatus.setTextColor(Color.parseColor("#B71C1C"))
                "INTERVIEW" -> h.tvStatus.setTextColor(Color.parseColor("#FFC107"))
                "OFFERED" -> h.tvStatus.setTextColor(Color.parseColor("#FF9800"))
                "HIRED" -> h.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                else -> h.tvStatus.setTextColor(Color.parseColor("#1565C0"))
            }

            if ("OFFERED" == app.status) {
                h.contactLayout.visibility = View.VISIBLE

                h.btnCall.setOnClickListener { v ->
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:" + app.employerMobile)
                    v.context.startActivity(intent)
                }

                h.btnWhatsApp.setOnClickListener { v ->
                    try {
                        val message = URLEncoder.encode(
                            "Hello, I'm interested in the job offer for the '" + app.jobTitle + "' position.",
                            "UTF-8"
                        )
                        val url = "https://wa.me/+91" + app.employerMobile + "?text=" + message
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        v.context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(v.context, "WhatsApp not installed or error.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                h.contactLayout.visibility = View.GONE
            }
        }

        override fun getItemCount(): Int = items.size

        internal class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvJobTitle: TextView = v.findViewById(R.id.tvJobTitle)
            val tvCompany: TextView = v.findViewById(R.id.tvCompany)
            val tvAppliedAt: TextView = v.findViewById(R.id.tvAppliedAt)
            val tvStatus: TextView = v.findViewById(R.id.tvStatus)
            val contactLayout: LinearLayout = v.findViewById(R.id.contactLayout)
            val btnCall: Button = v.findViewById(R.id.btnCall)
            val btnWhatsApp: Button = v.findViewById(R.id.btnWhatsApp)
        }
    }
}