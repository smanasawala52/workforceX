package com.workforcex.worker.ui.worker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.workforcex.worker.R;
import com.workforcex.worker.api.Verification;
import java.util.List;

public class VerificationStatusAdapter extends RecyclerView.Adapter<VerificationStatusAdapter.ViewHolder> {

    private final List<Verification> verifications;

    public VerificationStatusAdapter(List<Verification> verifications) {
        this.verifications = verifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_verification_status, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Verification verification = verifications.get(position);
        holder.tvType.setText(verification.verificationType);
        holder.tvStatus.setText(verification.status);
    }

    @Override
    public int getItemCount() {
        return verifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvVerificationType);
            tvStatus = itemView.findViewById(R.id.tvVerificationStatus);
        }
    }
}
