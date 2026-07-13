package com.workforcex.employer.ui.employer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.workforcex.employer.R
import com.workforcex.shared_employer.models.Document

class ReadOnlyDocumentAdapter(private val items: List<Document>) : RecyclerView.Adapter<ReadOnlyDocumentAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_document_readonly, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]
        h.tvDocumentType.text = item.documentType
        h.tvFileName.text = item.fileName
    }

    override fun getItemCount(): Int = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvDocumentType: TextView = v.findViewById(R.id.tvDocumentType)
        val tvFileName: TextView = v.findViewById(R.id.tvFileName)
    }
}
