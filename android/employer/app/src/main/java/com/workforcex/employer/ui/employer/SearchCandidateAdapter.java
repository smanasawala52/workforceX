package com.workforcex.employer.ui.employer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.workforcex.employer.R;
import com.workforcex.employer.api.CandidateSearchResult;
import java.util.List;

public class SearchCandidateAdapter extends RecyclerView.Adapter<SearchCandidateAdapter.ViewHolder> {

    private List<CandidateSearchResult> candidates;

    public SearchCandidateAdapter(List<CandidateSearchResult> candidates) {
        this.candidates = candidates;
    }

    public void update(List<CandidateSearchResult> newList) {
        this.candidates = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_candidate, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CandidateSearchResult c = candidates.get(position);

        h.tvName.setText((position + 1) + ". " + (c.name != null ? c.name : c.mobileNumber));
        h.tvTotalScore.setText(String.format("%.0f%%", c.totalScore));
        h.tvSkills.setText("Skills: " + (c.skills != null ? c.skills : "—"));

        String details = "";
        if (c.experience != null) details += c.experience + " yrs exp";
        if (c.city != null)       details += (details.isEmpty() ? "" : " · ") + c.city;
        if (c.preferredSalary != null) details += "  ₹" + c.preferredSalary.intValue();
        h.tvDetails.setText(details);

        h.tvSkillScore.setText(String.format("%.0f%%", c.skillScore));
        h.tvExpScore.setText(String.format("%.0f%%", c.experienceScore));
        h.tvLocScore.setText(String.format("%.0f%%", c.locationScore));
        h.tvSalScore.setText(String.format("%.0f%%", c.salaryScore));
    }

    @Override
    public int getItemCount() { return candidates.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTotalScore, tvSkills, tvDetails;
        TextView tvSkillScore, tvExpScore, tvLocScore, tvSalScore;

        ViewHolder(View v) {
            super(v);
            tvName       = v.findViewById(R.id.tvName);
            tvTotalScore = v.findViewById(R.id.tvTotalScore);
            tvSkills     = v.findViewById(R.id.tvSkills);
            tvDetails    = v.findViewById(R.id.tvDetails);
            tvSkillScore = v.findViewById(R.id.tvSkillScore);
            tvExpScore   = v.findViewById(R.id.tvExpScore);
            tvLocScore   = v.findViewById(R.id.tvLocScore);
            tvSalScore   = v.findViewById(R.id.tvSalScore);
        }
    }
}
