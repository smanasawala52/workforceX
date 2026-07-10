package com.workforcex.worker.ui.worker

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.workforcex.shared.models.JobApplication
import com.workforcex.shared.models.JobBrowseItem
import com.workforcex.shared.RetrofitClient
import com.workforcex.worker.databinding.ActivityBrowseJobsBinding
import com.workforcex.worker.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.stream.Collectors

class BrowseJobsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBrowseJobsBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var adapter: BrowseJobsAdapter
    private var allJobs: List<JobBrowseItem> = ArrayList()
    private val appliedJobIds: MutableSet<String> = HashSet()
    private var filterSkills: String? = null
    private var filterCity: String? = null
    private var showMatchedFirst = false
    private var currentlyShowingMatched = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBrowseJobsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        adapter = BrowseJobsAdapter(ArrayList(), this::applyToJob, appliedJobIds)
        binding.rvJobs.layoutManager = LinearLayoutManager(this)
        binding.rvJobs.adapter = adapter

        filterSkills = intent.getStringExtra("filterSkills")
        filterCity = intent.getStringExtra("filterCity")
        showMatchedFirst = intent.getBooleanExtra("showMatchedFirst", false)

        if (showMatchedFirst && !filterSkills.isNullOrEmpty()) {
            title = "Jobs Matching Your Skills"
            binding.btnToggleJobs.visibility = View.VISIBLE
            binding.btnToggleJobs.text = "Show All Jobs"
            binding.tvFilterInfo.visibility = View.VISIBLE
            binding.tvFilterInfo.text = "Matching: $filterSkills"
        } else {
            title = "Available Jobs"
            binding.btnToggleJobs.visibility = View.GONE
            binding.tvFilterInfo.visibility = View.GONE
            currentlyShowingMatched = false
        }

        binding.btnToggleJobs.setOnClickListener { toggleJobsView() }
        loadInitialData()
    }

    private fun loadInitialData() {
        binding.progressBar.visibility = View.VISIBLE
        // First, get the jobs this worker has already applied to
        RetrofitClient.get().getMyApplications(tokenManager.getBearerToken())
            .enqueue(object : Callback<List<JobApplication>> {
                override fun onResponse(
                    call: Call<List<JobApplication>>,
                    response: Response<List<JobApplication>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        appliedJobIds.clear()
                        val newIds = response.body()!!.stream()
                            .map { app: JobApplication -> app.jobId }
                            .collect(Collectors.toSet())
                        appliedJobIds.addAll(newIds)
                    }
                    // Then, load all jobs
                    loadJobs()
                }

                override fun onFailure(call: Call<List<JobApplication>>, t: Throwable) {
                    // Still load jobs even if this fails
                    loadJobs()
                }
            })
    }

    private fun loadJobs() {
        RetrofitClient.get().browseJobs(tokenManager.getBearerToken())
            .enqueue(object : Callback<List<JobBrowseItem>> {
                override fun onResponse(
                    call: Call<List<JobBrowseItem>>,
                    response: Response<List<JobBrowseItem>>
                ) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        allJobs = response.body()!!
                        displayJobs(currentlyShowingMatched && showMatchedFirst)
                    }
                }

                override fun onFailure(call: Call<List<JobBrowseItem>>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@BrowseJobsActivity,
                        "Network error: " + t.message, Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun toggleJobsView() {
        currentlyShowingMatched = !currentlyShowingMatched
        displayJobs(currentlyShowingMatched)
        binding.btnToggleJobs.text = if (currentlyShowingMatched) "Show All Jobs" else "Show Matching Jobs"
        binding.tvFilterInfo.visibility = if (currentlyShowingMatched) View.VISIBLE else View.GONE
        title = if (currentlyShowingMatched) "Jobs Matching Your Skills" else "All Jobs (${allJobs.size})"
    }

    private fun displayJobs(matchedOnly: Boolean) {
        val toShow = if (matchedOnly && !filterSkills.isNullOrEmpty())
            filterBySkills(allJobs, filterSkills, filterCity)
        else
            allJobs
        adapter.update(toShow)
        binding.tvEmpty.visibility = if (toShow.isEmpty()) View.VISIBLE else View.GONE
        binding.tvResultCount.visibility = View.VISIBLE
        binding.tvResultCount.text = (toShow.size.toString() +
                if (matchedOnly) " job(s) match your skills" else " total jobs available")
    }

    private fun applyToJob(job: JobBrowseItem, position: Int) {
        // Disable button immediately to prevent double-clicks
        job.applied = true
        adapter.notifyItemChanged(position)

        RetrofitClient.get().applyToJob(tokenManager.getBearerToken(), job.id)
            .enqueue(object : Callback<JobApplication> {
                override fun onResponse(
                    call: Call<JobApplication>,
                    response: Response<JobApplication>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@BrowseJobsActivity,
                            "Applied to " + job.title + "! ✓", Toast.LENGTH_SHORT
                        ).show()
                        appliedJobIds.add(job.id) // Add to our set
                    } else {
                        Toast.makeText(
                            this@BrowseJobsActivity,
                            "Failed to apply", Toast.LENGTH_SHORT
                        ).show()
                        // Re-enable button if the call failed
                        job.applied = false
                        adapter.notifyItemChanged(position)
                    }
                }

                override fun onFailure(call: Call<JobApplication>, t: Throwable) {
                    Toast.makeText(
                        this@BrowseJobsActivity,
                        "Network error", Toast.LENGTH_SHORT
                    ).show()
                    // Re-enable button on failure
                    job.applied = false
                    adapter.notifyItemChanged(position)
                }
            })
    }

    private fun filterBySkills(jobs: List<JobBrowseItem>, skills: String?, city: String?): List<JobBrowseItem> {
        val workerSkills = HashSet(Arrays.asList(*skills!!.lowercase().split(",").toTypedArray()))
        return jobs.stream().filter { job: JobBrowseItem ->
            if (job.skillsRequired == null) return@filter false
            val jobSkills = HashSet(Arrays.asList(*job.skillsRequired.lowercase().split(",").toTypedArray()))
            val match = jobSkills.stream().anyMatch { s: String? -> workerSkills.contains(s) }
            if (!match) return@filter false
            if (!city.isNullOrEmpty() && job.location != null)
                return@filter job.location.lowercase(Locale.getDefault()).contains(city.lowercase(Locale.getDefault()))
            true
        }.collect(Collectors.toList())
    }
}