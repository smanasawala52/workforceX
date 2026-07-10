package com.workforcex.employer.ui.employer

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.workforcex.shared_employer.models.JobRequest
import com.workforcex.shared_employer.models.JobResponse
import com.workforcex.shared_employer.RetrofitClient
import com.workforcex.shared_employer.models.Skill
import com.workforcex.employer.databinding.ActivityJobCreateEditBinding
import com.workforcex.employer.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.stream.Collectors

class JobCreateEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobCreateEditBinding
    private lateinit var tokenManager: TokenManager
    private var jobId: String? = null

    private val selectedSkills = LinkedHashSet<String>()
    private var allSkills: List<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobCreateEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenManager = TokenManager(this)

        jobId = intent.getStringExtra("jobId")
        val isEditing = jobId != null
        title = if (isEditing) "Edit Job" else "Create Job"

        fetchSkillsAndSetupAutocomplete()

        if (isEditing) {
            binding.etTitle.setText(intent.getStringExtra("jobTitle"))
            addSkillsFromCsv(intent.getStringExtra("jobSkills"))
            val exp = intent.getIntExtra("jobExperience", 0)
            if (exp > 0) binding.etExperience.setText(exp.toString())
            binding.etLocation.setText(intent.getStringExtra("jobLocation"))
            val salMin = intent.getDoubleExtra("jobSalaryMin", 0.0)
            val salMax = intent.getDoubleExtra("jobSalaryMax", 0.0)
            if (salMin > 0) binding.etSalaryMin.setText(salMin.toInt().toString())
            if (salMax > 0) binding.etSalaryMax.setText(salMax.toInt().toString())
            val positions = intent.getIntExtra("jobOpenPositions", 0)
            if (positions > 0) binding.etOpenPositions.setText(positions.toString())
            binding.etDescription.setText(intent.getStringExtra("jobDescription"))
        }

        binding.btnSave.text = if (isEditing) "Update Job" else "Post Job"
        binding.btnSave.setOnClickListener { saveJob() }
    }

    private fun fetchSkillsAndSetupAutocomplete() {
        RetrofitClient.get().getSkills().enqueue(object : Callback<List<Skill>> {
            override fun onResponse(call: Call<List<Skill>>, response: Response<List<Skill>>) {
                if (response.isSuccessful && response.body() != null) {
                    allSkills = response.body()!!.stream().map { s: Skill -> s.name }.collect(Collectors.toList())
                    setupSkillsAutocomplete()
                }
            }

            override fun onFailure(call: Call<List<Skill>>, t: Throwable) {}
        })
    }

    private fun setupSkillsAutocomplete() {
        val adapter = ArrayAdapter(
            this, android.R.layout.simple_dropdown_item_1line, allSkills
        )
        binding.acSkills.setAdapter(adapter)

        binding.acSkills.setOnItemClickListener { parent, _, position, _ ->
            val skill = parent.getItemAtPosition(position) as String
            addSkillChip(skill)
            binding.acSkills.setText("")
        }

        binding.acSkills.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val typed = binding.acSkills.text.toString().trim().lowercase()
                if (typed.isNotEmpty()) {
                    addSkillChip(typed)
                    binding.acSkills.setText("")
                }
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun addSkillChip(skill: String) {
        if (selectedSkills.size >= 5) {
            Toast.makeText(this, "You can add a maximum of 5 skills", Toast.LENGTH_SHORT).show()
            return
        }
        val clean = skill.trim().lowercase()
        if (clean.isEmpty() || selectedSkills.contains(clean)) return

        selectedSkills.add(clean)

        val chip = Chip(this)
        chip.text = clean
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            selectedSkills.remove(clean)
            binding.chipGroupSkills.removeView(chip)
        }
        binding.chipGroupSkills.addView(chip)
    }

    private fun addSkillsFromCsv(csv: String?) {
        if (csv.isNullOrBlank()) return
        for (skill in csv.split(",")) {
            addSkillChip(skill.trim())
        }
    }

    private fun getSkillsCsv(): String {
        return selectedSkills.joinToString(",")
    }

    private fun saveJob() {
        val title = binding.etTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.etTitle.error = "Title is required"
            return
        }

        val request = JobRequest(
            title = title,
            skillsRequired = getSkillsCsv(),
            experienceRequired = binding.etExperience.text.toString().trim().toIntOrNull() ?: 0,
            location = binding.etLocation.text.toString().trim(),
            salaryMin = binding.etSalaryMin.text.toString().trim().toDoubleOrNull() ?: 0.0,
            salaryMax = binding.etSalaryMax.text.toString().trim().toDoubleOrNull() ?: 0.0,
            openPositions = binding.etOpenPositions.text.toString().trim().toIntOrNull() ?: 0,
            description = binding.etDescription.text.toString().trim()
        )

        setLoading(true)

        val callback = object : Callback<JobResponse> {
            override fun onResponse(call: Call<JobResponse>, response: Response<JobResponse>) {
                setLoading(false)
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@JobCreateEditActivity,
                        if (jobId != null) "Job updated!" else "Job posted!", Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(this@JobCreateEditActivity, "Failed to save job", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<JobResponse>, t: Throwable) {
                setLoading(false)
                Toast.makeText(this@JobCreateEditActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        }

        if (jobId != null) {
            RetrofitClient.get().updateJob(tokenManager.getBearerToken(), jobId!!, request).enqueue(callback)
        } else {
            RetrofitClient.get().createJob(tokenManager.getBearerToken(), request).enqueue(callback)
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !loading
    }
}