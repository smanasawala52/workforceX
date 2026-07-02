package com.workforcex.employer.ui.employer;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.workforcex.employer.api.CandidateSearchResult;
import com.workforcex.employer.api.RetrofitClient;
import com.workforcex.employer.databinding.ActivitySearchCandidatesBinding;
import com.workforcex.employer.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class SearchCandidatesActivity extends AppCompatActivity {

    private ActivitySearchCandidatesBinding binding;
    private TokenManager tokenManager;
    private SearchCandidateAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchCandidatesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Search Candidates");

        tokenManager = new TokenManager(this);
        adapter = new SearchCandidateAdapter(new ArrayList<>());
        binding.rvCandidates.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCandidates.setAdapter(adapter);

        binding.btnSearch.setOnClickListener(v -> performSearch());

        // Run an open search immediately on open so employer sees all candidates
        performSearch();
    }

    private void performSearch() {
        String skills    = text(binding.etSkills);
        String city      = text(binding.etCity);
        Integer expMin   = intVal(binding.etExpMin);
        Integer expMax   = intVal(binding.etExpMax);
        Double salMin    = doubleVal(binding.etSalaryMin);
        Double salMax    = doubleVal(binding.etSalaryMax);

        setLoading(true);

        RetrofitClient.get().searchCandidates(
                tokenManager.getBearerToken(),
                skills.isEmpty() ? null : skills,
                city.isEmpty()   ? null : city,
                expMin, expMax, salMin, salMax
        ).enqueue(new Callback<List<CandidateSearchResult>>() {
            @Override
            public void onResponse(Call<List<CandidateSearchResult>> call,
                                   Response<List<CandidateSearchResult>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<CandidateSearchResult> results = response.body();
                    adapter.update(results);
                    binding.tvResultCount.setVisibility(View.VISIBLE);
                    binding.tvResultCount.setText(results.size() + " candidate(s) found");
                    binding.tvEmpty.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(SearchCandidatesActivity.this,
                            "Search failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CandidateSearchResult>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(SearchCandidatesActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String text(com.google.android.material.textfield.TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private Integer intVal(com.google.android.material.textfield.TextInputEditText et) {
        String s = text(et);
        return s.isEmpty() ? null : Integer.parseInt(s);
    }

    private Double doubleVal(com.google.android.material.textfield.TextInputEditText et) {
        String s = text(et);
        return s.isEmpty() ? null : Double.parseDouble(s);
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSearch.setEnabled(!loading);
    }
}
