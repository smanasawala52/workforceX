package com.workforcex.employer.ui.employer

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.workforcex.employer.R
import com.workforcex.shared_employer.models.CandidateSearchResult

class SearchCandidateAdapter(private var candidates: List<CandidateSearchResult>) :
    RecyclerView.Adapter<SearchCandidateAdapter.ViewHolder>() {

    fun update(newList: List<CandidateSearchResult>) {
        this.candidates = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_candidate, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(h: ViewHolder, position: Int) {
        val c = candidates[position]

        h.tvName.text = (position + 1).toString() + ". " + (c.name ?: c.mobileNumber)
        h.tvTotalScore.text = String.format("%.0f%%", c.totalScore)
        h.tvSkills.text = "Skills: " + (c.skills ?: "—")

        var details = ""
        if (c.experience != null) details += c.experience.toString() + " yrs exp"
        if (c.city != null) details += (if (details.isEmpty()) "" else " · ") + c.city
        if (c.preferredSalary != null) details += "  ₹" + c.preferredSalary.toInt()
        h.tvDetails.text = details

        h.tvSkillScore.text = String.format("%.0f%%", c.skillScore)
        h.tvExpScore.text = String.format("%.0f%%", c.experienceScore)
        h.tvLocScore.text = String.format("%.0f%%", c.locationScore)
        h.tvSalScore.text = String.format("%.0f%%", c.salaryScore)

        h.itemView.setOnClickListener {
            val intent = Intent(h.itemView.context, CandidateProfileActivity::class.java)
            intent.putExtra("workerId", c.workerId)
            h.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = candidates.size

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvTotalScore: TextView = v.findViewById(R.id.tvTotalScore)
        val tvSkills: TextView = v.findViewById(R.id.tvSkills)
        val tvDetails: TextView = v.findViewById(R.id.tvDetails)
        val tvSkillScore: TextView = v.findViewById(R.id.tvSkillScore)
        val tvExpScore: TextView = v.findViewById(R.id.tvExpScore)
        val tvLocScore: TextView = v.findViewById(R.id.tvLocScore)
        val tvSalScore: TextView = v.findViewById(R.id.tvSalScore)
    }
}