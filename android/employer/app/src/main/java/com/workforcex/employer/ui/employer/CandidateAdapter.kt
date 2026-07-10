package com.workforcex.employer.ui.employer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.workforcex.employer.R
import com.workforcex.shared_employer.models.MatchedWorker

class CandidateAdapter(private val workers: List<MatchedWorker>) :
    RecyclerView.Adapter<CandidateAdapter.CandidateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_candidate, parent, false)
        return CandidateViewHolder(view)
    }

    override fun onBindViewHolder(holder: CandidateViewHolder, position: Int) {
        val w = workers[position]
        holder.tvRank.text = "#" + (position + 1)
        holder.tvName.text = w.name.ifBlank { w.mobileNumber }
        holder.tvScore.text = String.format("Score: %.0f%%", w.score)
        holder.tvSkills.text = "Skills: " + w.skills.ifBlank { "—" }
        holder.tvExperience.text = "Experience: " + w.experience + " yrs"
        holder.tvCity.text = "Location: " + w.city.ifBlank { "—" }
        holder.tvSalary.text = "Expected: ₹" + w.preferredSalary.toInt()
    }

    override fun getItemCount(): Int = workers.size

    class CandidateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvScore: TextView = itemView.findViewById(R.id.tvScore)
        val tvSkills: TextView = itemView.findViewById(R.id.tvSkills)
        val tvExperience: TextView = itemView.findViewById(R.id.tvExperience)
        val tvCity: TextView = itemView.findViewById(R.id.tvCity)
        val tvSalary: TextView = itemView.findViewById(R.id.tvSalary)
    }
}
