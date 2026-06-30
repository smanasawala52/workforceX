package com.workforcex.android.ui.employer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.workforcex.android.databinding.ActivityJobsBinding;

/**
 * Full implementation coming in the next milestone once Matching Engine is done.
 * For now: screen opens without crashing, Employer dashboard button works.
 */
public class JobsActivity extends AppCompatActivity {

    private ActivityJobsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJobsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
