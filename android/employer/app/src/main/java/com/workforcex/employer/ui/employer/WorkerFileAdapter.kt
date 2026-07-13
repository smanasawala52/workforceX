package com.workforcex.employer.ui.employer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.workforcex.employer.R
import com.workforcex.shared_employer.models.Document

/**
 * Shows the actual documents a worker uploaded (filename + type + a way to
 * open the file) - this is what makes the employer's worker-profile view
 * show the same documents the worker sees in their own app, as opposed to
 * [DocumentAdapter] which (despite its name) only shows the coarse
 * per-category verification status used for approve/reject.
 */
class WorkerFileAdapter(
    private val items: List<Document>,
    private val onView: (Document) -> Unit
) : RecyclerView.Adapter<WorkerFileAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_uploaded_file, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvDocumentType.text = item.documentType
        holder.tvFileName.text = item.fileName
        holder.btnViewDocument.setOnClickListener { onView(item) }
    }

    override fun getItemCount(): Int = items.size

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvDocumentType: TextView = view.findViewById(R.id.tvDocumentType)
        val tvFileName: TextView = view.findViewById(R.id.tvFileName)
        val btnViewDocument: Button = view.findViewById(R.id.btnViewDocument)
    }
}
