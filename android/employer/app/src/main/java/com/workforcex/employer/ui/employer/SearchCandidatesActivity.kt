package com.workforcex.employer.ui.employer

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.workforcex.shared_employer.models.CandidateSearchRequest
import com.workforcex.shared_employer.models.CandidateSearchResult
import com.workforcex.shared_employer.RetrofitClient
import com.workforcex.employer.databinding.ActivitySearchCandidatesBinding
import com.workforcex.employer.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchCandidatesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchCandidatesBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var adapter: SearchCandidateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchCandidatesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Search Candidates"

        tokenManager = TokenManager(this)
        adapter = SearchCandidateAdapter(ArrayList())
        binding.rvCandidates.layoutManager = LinearLayoutManager(this)
        binding.rvCandidates.adapter = adapter

        binding.btnSearch.setOnClickListener { performSearch() }
        performSearch()
    }

    private fun performSearch() {
        val request = CandidateSearchRequest(
            skills = text(binding.etSkills).ifEmpty { "" },
            city = text(binding.etCity).ifEmpty { "" },
            experienceMin = intVal(binding.etExpMin) ?: 0,
            experienceMax = intVal(binding.etExpMax) ?: 100,
            salaryMin = doubleVal(binding.etSalaryMin) ?: 0.0,
            salaryMax = doubleVal(binding.etSalaryMax) ?: 1000000.0
        )

        setLoading(true)

        RetrofitClient.get().searchCandidates(tokenManager.getBearerToken(), request)
            .enqueue(object : Callback<List<CandidateSearchResult>> {
                override fun onResponse(
                    call: Call<List<CandidateSearchResult>>,
                    response: Response<List<CandidateSearchResult>>
                ) {
                    setLoading(false)
                    if (response.isSuccessful && response.body() != null) {
                        val results = response.body()!!
                        adapter.update(results)
                        binding.tvResultCount.visibility = View.VISIBLE
                        binding.tvResultCount.text = results.size.toString() + " candidate(s) found"
                        binding.tvEmpty.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
                    } else {
                        Toast.makeText(this@SearchCandidatesActivity, "Search failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<CandidateSearchResult>>, t: Throwable) {
                    setLoading(false)
                    Toast.makeText(
                        this@SearchCandidatesActivity,
                        "Network error: " + t.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun text(et: TextInputEditText): String {
        return et.text?.toString()?.trim() ?: ""
    }

    private fun intVal(et: TextInputEditText): Int? {
        val s = text(et)
        return s.toIntOrNull()
    }

    private fun doubleVal(et: TextInputEditText): Double? {
        val s = text(et)
        return s.toDoubleOrNull()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSearch.isEnabled = !loading
    }
}