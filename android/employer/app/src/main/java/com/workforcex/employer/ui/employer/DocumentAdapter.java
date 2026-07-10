package com.workforcex.employer.ui.employer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.workforcex.employer.R;
import com.workforcex.employer.api.Verification;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.VH> {

    private final List<Verification> items;
    private final OnVerificationAction onAction;

    public interface OnVerificationAction {
        void onUpdate(String verificationId, String status, String comments);
    }

    public DocumentAdapter(List<Verification> items, OnVerificationAction onAction) {
        this.items = items;
        this.onAction = onAction;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Verification item = items.get(pos);
        h.tvDocumentType.setText(item.verificationType);
        h.tvStatus.setText(item.status);

        h.btnApprove.setOnClickListener(v -> onAction.onUpdate(item.id, "VERIFIED", ""));
        h.btnReject.setOnClickListener(v -> onAction.onUpdate(item.id, "REJECTED", ""));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDocumentType, tvStatus;
        Button btnApprove, btnReject;

        VH(View v) {
            super(v);
            tvDocumentType = v.findViewById(R.id.tvDocumentType);
            tvStatus = v.findViewById(R.id.tvStatus);
            btnApprove = v.findViewById(R.id.btnApprove);
            btnReject = v.findViewById(R.id.btnReject);
        }
    }
}
