package com.workforcex.worker.ui.worker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.workforcex.shared.RetrofitClient
import com.workforcex.shared.models.Skill
import com.workforcex.shared.models.WorkerProfileRequest
import com.workforcex.shared.models.WorkerProfileResponse
import com.workforcex.worker.databinding.ActivityWorkerProfileBinding
import com.workforcex.worker.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.stream.Collectors

class WorkerProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkerProfileBinding
    private lateinit var tokenManager: TokenManager

    private val selectedSkills = LinkedHashSet<String>()
    private var allSkills: List<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "My Profile"
        tokenManager = TokenManager(this)

        fetchSkillsAndSetupAutocomplete()
        loadExistingProfile()
        binding.btnSave.setOnClickListener { saveProfile() }
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

    private fun loadExistingProfile() {
        RetrofitClient.get().getWorkerProfile(tokenManager.getBearerToken())
            .enqueue(object : Callback<WorkerProfileResponse> {
                override fun onResponse(
                    call: Call<WorkerProfileResponse>,
                    response: Response<WorkerProfileResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val p = response.body()!!
                        if (p.name != null) binding.etName.setText(p.name)
                        if (p.city != null) binding.etCity.setText(p.city)
                        if (p.state != null) binding.etState.setText(p.state)
                        if (p.skills != null) addSkillsFromCsv(p.skills)
                        if (p.experience != null) binding.etExperience.setText(p.experience.toString())
                        if (p.preferredSalary != null) binding.etSalary.setText(p.preferredSalary.toInt().toString())
                        if (p.availability != null) binding.etAvailability.setText(p.availability)
                        if (p.languages != null) binding.etLanguages.setText(p.languages)
                    }
                }

                override fun onFailure(call: Call<WorkerProfileResponse>, t: Throwable) {}
            })
    }

    private fun saveProfile() {
        val request = WorkerProfileRequest(
            name = binding.etName.text.toString().trim(),
            gender = "",
            city = binding.etCity.text.toString().trim(),
            state = binding.etState.text.toString().trim(),
            skills = getSkillsCsv(),
            experience = binding.etExperience.text.toString().trim().toIntOrNull() ?: 0,
            preferredSalary = binding.etSalary.text.toString().trim().toDoubleOrNull() ?: 0.0,
            availability = binding.etAvailability.text.toString().trim(),
            languages = binding.etLanguages.text.toString().trim()
        )

        setLoading(true)

        RetrofitClient.get().saveWorkerProfile(tokenManager.getBearerToken(), request)
            .enqueue(object : Callback<WorkerProfileResponse> {
                override fun onResponse(
                    call: Call<WorkerProfileResponse>,
                    response: Response<WorkerProfileResponse>
                ) {
                    setLoading(false)
                    if (response.isSuccessful) {
                        showMatchedJobs(request.skills, request.city)
                    } else {
                        Toast.makeText(this@WorkerProfileActivity, "Failed to save profile", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<WorkerProfileResponse>, t: Throwable) {
                    setLoading(false)
                    Toast.makeText(this@WorkerProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showMatchedJobs(skills: String, city: String) {
        val intent = Intent(this, BrowseJobsActivity::class.java)
        intent.putExtra("filterSkills", skills)
        intent.putExtra("filterCity", city)
        intent.putExtra("showMatchedFirst", true)
        startActivity(intent)
        finish()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !loading
    }
}