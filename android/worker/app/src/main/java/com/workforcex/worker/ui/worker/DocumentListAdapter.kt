package com.workforcex.worker.ui.worker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.workforcex.shared.models.Document
import com.workforcex.worker.R

/**
 * Shows the worker's own uploaded documents (filename + type + a way to open
 * the file) - this is what actually backs "list of documents uploaded",
 * as opposed to VerificationStatusAdapter which only shows the coarse
 * per-category status (Identity/Skill/... = Pending/Submitted/Verified).
 */
class DocumentListAdapter(
    private val items: List<Document>,
    private val onView: (Document) -> Unit
) : RecyclerView.Adapter<DocumentListAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_document, parent, false)
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
