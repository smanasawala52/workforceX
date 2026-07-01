package com.workforcex.employer.ui.employer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.workforcex.employer.R;
import com.workforcex.employer.api.MatchedWorker;
import java.util.List;

public class CandidateAdapter extends RecyclerView.Adapter<CandidateAdapter.CandidateViewHolder> {

    private final List<MatchedWorker> workers;

    public CandidateAdapter(List<MatchedWorker> workers) {
        this.workers = workers;
    }

    @NonNull
    @Override
    public CandidateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_candidate, parent, false);
        return new CandidateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CandidateViewHolder holder, int position) {
        MatchedWorker w = workers.get(position);
        holder.tvRank.setText("#" + (position + 1));
        holder.tvName.setText(w.name != null ? w.name : w.mobileNumber);
        holder.tvScore.setText(String.format("Score: %.0f%%", w.score));
        holder.tvSkills.setText("Skills: " + (w.skills != null ? w.skills : "—"));
        holder.tvExperience.setText("Experience: " + (w.experience != null ? w.experience + " yrs" : "—"));
        holder.tvCity.setText("Location: " + (w.city != null ? w.city : "—"));
        holder.tvSalary.setText(w.preferredSalary != null ? "Expected: ₹" + w.preferredSalary.intValue() : "");
    }

    @Override
    public int getItemCount() { return workers.size(); }

    static class CandidateViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvScore, tvSkills, tvExperience, tvCity, tvSalary;

        CandidateViewHolder(View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvName = itemView.findViewById(R.id.tvName);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvSkills = itemView.findViewById(R.id.tvSkills);
            tvExperience = itemView.findViewById(R.id.tvExperience);
            tvCity = itemView.findViewById(R.id.tvCity);
            tvSalary = itemView.findViewById(R.id.tvSalary);
        }
    }
}
