package com.workforcex.worker.ui.worker;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.workforcex.worker.api.ResumeParseResult;
import com.workforcex.worker.api.RetrofitClient;
import com.workforcex.worker.api.WorkerProfileResponse;
import com.workforcex.worker.databinding.ActivityWorkerDashboardBinding;
import com.workforcex.worker.ui.auth.LoginActivity;
import com.workforcex.worker.utils.TokenManager;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkerDashboardActivity extends AppCompatActivity {

    private ActivityWorkerDashboardBinding binding;
    private TokenManager tokenManager;

    // File picker for PDF resume
    private final ActivityResultLauncher<String[]> filePicker =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) uploadResume(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorkerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Worker Dashboard");
        tokenManager = new TokenManager(this);

        binding.btnBrowseJobs.setOnClickListener(v ->
                startActivity(new Intent(this, BrowseJobsActivity.class)));

        binding.btnMyApplications.setOnClickListener(v ->
                startActivity(new Intent(this, MyApplicationsActivity.class)));

        binding.btnNotifications.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        binding.btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, WorkerProfileActivity.class)));

        binding.btnUploadResume.setOnClickListener(v ->
                filePicker.launch(new String[]{"application/pdf"}));

        binding.btnLogout.setOnClickListener(v -> logout());

        loadProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile(); // refresh after returning from profile edit
    }

    private void loadProfile() {
        RetrofitClient.get().getWorkerProfile(tokenManager.getBearerToken())
                .enqueue(new Callback<WorkerProfileResponse>() {
                    @Override
                    public void onResponse(Call<WorkerProfileResponse> call,
                                           Response<WorkerProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            WorkerProfileResponse p = response.body();
                            String name = (p.name != null && !p.name.isEmpty())
                                    ? p.name : tokenManager.getMobile();
                            binding.tvWelcome.setText("Welcome, " + name);
                            binding.tvSkills.setText("Skills: " +
                                    (p.skills != null ? p.skills : "Not set — edit your profile"));
                            binding.tvExperience.setText("Experience: " +
                                    (p.experience != null ? p.experience + " yrs" : "Not set"));
                        } else {
                            binding.tvWelcome.setText("Welcome, " + tokenManager.getMobile());
                            binding.tvSkills.setText("Skills: Not set — edit your profile");
                            binding.tvExperience.setText("Experience: Not set");
                        }
                    }
                    @Override
                    public void onFailure(Call<WorkerProfileResponse> call, Throwable t) {
                        binding.tvWelcome.setText("Welcome, " + tokenManager.getMobile());
                    }
                });
    }

    private void uploadResume(Uri uri) {
        try {
            String fileName = getFileName(uri);
            InputStream inputStream = getContentResolver().openInputStream(uri);
            byte[] bytes = inputStream.readAllBytes();
            inputStream.close();

            RequestBody requestBody = RequestBody.create(bytes,
                    MediaType.parse("application/pdf"));
            MultipartBody.Part part = MultipartBody.Part.createFormData(
                    "file", fileName, requestBody);

            binding.btnUploadResume.setEnabled(false);
            binding.btnUploadResume.setText("Uploading...");

            RetrofitClient.get().uploadResume(tokenManager.getBearerToken(), part)
                    .enqueue(new Callback<ResumeParseResult>() {
                        @Override
                        public void onResponse(Call<ResumeParseResult> call,
                                               Response<ResumeParseResult> response) {
                            binding.btnUploadResume.setEnabled(true);
                            binding.btnUploadResume.setText("Upload Resume (PDF)");
                            if (response.isSuccessful() && response.body() != null) {
                                showResumeResult(response.body());
                                loadProfile(); // refresh skills
                            } else {
                                Toast.makeText(WorkerDashboardActivity.this,
                                        "Upload failed. Only PDF files are accepted.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<ResumeParseResult> call, Throwable t) {
                            binding.btnUploadResume.setEnabled(true);
                            binding.btnUploadResume.setText("Upload Resume (PDF)");
                            Toast.makeText(WorkerDashboardActivity.this,
                                    "Network error: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Failed to read file: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showResumeResult(ResumeParseResult result) {
        String skillsText = (result.detectedSkillList != null && !result.detectedSkillList.isEmpty())
                ? String.join(", ", result.detectedSkillList)
                : "None detected";

        String expText = result.detectedExperience != null
                ? result.detectedExperience + " years"
                : "Not detected";

        new AlertDialog.Builder(this)
                .setTitle("Resume Parsed!")
                .setMessage(
                        result.message + "\n\n" +
                        "Skills found: " + skillsText + "\n" +
                        "Experience: " + expText + "\n\n" +
                        "Your profile has been updated automatically. " +
                        "Tap 'Edit Profile' to review and adjust.")
                .setPositiveButton("Browse Matching Jobs", (d, w) -> {
                    Intent intent = new Intent(this, BrowseJobsActivity.class);
                    intent.putExtra("filterSkills", result.extractedSkills);
                    intent.putExtra("showMatchedFirst", true);
                    startActivity(intent);
                })
                .setNeutralButton("OK", null)
                .show();
    }

    private String getFileName(Uri uri) {
        String result = "resume.pdf";
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) result = cursor.getString(idx);
            }
        }
        return result;
    }

    private void logout() {
        tokenManager.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
