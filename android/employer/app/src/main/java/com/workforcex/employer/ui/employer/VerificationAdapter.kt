package com.workforcex.employer.ui.employer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.workforcex.employer.R
import com.workforcex.shared_employer.models.Verification

class VerificationAdapter(
    private val items: List<Verification>,
    private val onAction: OnVerificationAction
) : RecyclerView.Adapter<VerificationAdapter.VH>() {

    fun interface OnVerificationAction {
        fun onUpdate(verificationId: String, status: String, comments: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_verification, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]
        h.tvWorkerName.text = "Worker: " + item.user.mobileNumber
        h.tvVerificationType.text = "Type: " + item.verificationType

        h.btnApprove.setOnClickListener { onAction.onUpdate(item.id, "VERIFIED", "") }
        h.btnReject.setOnClickListener { onAction.onUpdate(item.id, "REJECTED", "") }
    }

    override fun getItemCount(): Int = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvWorkerName: TextView = v.findViewById(R.id.tvWorkerName)
        val tvVerificationType: TextView = v.findViewById(R.id.tvVerificationType)
        val btnApprove: Button = v.findViewById(R.id.btnApprove)
        val btnReject: Button = v.findViewById(R.id.btnReject)
    }
}
