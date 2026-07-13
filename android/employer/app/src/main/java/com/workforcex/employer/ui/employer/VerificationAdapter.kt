package com.workforcex.employer.ui.employer

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.workforcex.employer.R
import com.workforcex.shared_employer.models.Verification

class VerificationAdapter(
    private val items: List<Verification>
) : RecyclerView.Adapter<VerificationAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_verification, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]
        h.tvWorkerName.text = "Worker: " + (item.user.name ?: item.user.mobileNumber)
        h.tvVerificationType.text = "Type: " + item.verificationType

        h.btnView.setOnClickListener {
            val context = h.itemView.context
            val intent = Intent(context, VerifyWorkerActivity::class.java).apply {
                putExtra("workerId", item.user.id)
                putExtra("workerName", item.user.name)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvWorkerName: TextView = v.findViewById(R.id.tvWorkerName)
        val tvVerificationType: TextView = v.findViewById(R.id.tvVerificationType)
        val btnView: Button = v.findViewById(R.id.btnView)
    }
}
