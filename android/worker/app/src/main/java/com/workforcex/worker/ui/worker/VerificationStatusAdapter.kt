package com.workforcex.worker.ui.worker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.workforcex.worker.R
import com.workforcex.shared.models.Verification

class VerificationStatusAdapter(private val verifications: List<Verification>) :
    RecyclerView.Adapter<VerificationStatusAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_verification_status, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val verification = verifications[position]
        holder.tvType.text = verification.verificationType
        holder.tvStatus.text = verification.status
    }

    override fun getItemCount(): Int {
        return verifications.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvType: TextView = itemView.findViewById(R.id.tvVerificationType)
        val tvStatus: TextView = itemView.findViewById(R.id.tvVerificationStatus)
    }
}