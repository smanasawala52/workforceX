package com.workforcex.worker.ui.worker;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.workforcex.worker.api.RetrofitClient;
import com.workforcex.worker.api.Verification;
import com.workforcex.worker.databinding.ActivityVerificationBinding;
import com.workforcex.worker.utils.TokenManager;
import java.io.InputStream;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerificationActivity extends AppCompatActivity {

    private ActivityVerificationBinding binding;
    private TokenManager tokenManager;
    private Uri selectedFileUri;

    private final ActivityResultLauncher<String> filePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    binding.tvSelectedFile.setText("Selected: " + uri.getLastPathSegment());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Verification Center");
        tokenManager = new TokenManager(this);

        binding.rvVerificationStatus.setLayoutManager(new LinearLayoutManager(this));
        setupDocumentSpinner();
        loadVerificationStatus();

        binding.btnChooseFile.setOnClickListener(v -> {
            if (binding.spinnerDocumentType.getSelectedItem() != null) {
                filePicker.launch("*/*");
            } else {
                Toast.makeText(this, "Please select a document type first", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnUpload.setOnClickListener(v -> uploadDocument());
    }

    private void setupDocumentSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"AADHAAR", "PAN", "DRIVING_LICENSE", "PASSPORT", "CERTIFICATION", "PHOTO", "INTRO_VIDEO"});
        binding.spinnerDocumentType.setAdapter(adapter);
    }

    private void loadVerificationStatus() {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.get().getVerificationStatus(tokenManager.getBearerToken())
                .enqueue(new Callback<List<Verification>>() {
                    @Override
                    public void onResponse(Call<List<Verification>> call, Response<List<Verification>> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            binding.rvVerificationStatus.setAdapter(new VerificationStatusAdapter(response.body()));
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Verification>> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void uploadDocument() {
        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        String docType = binding.spinnerDocumentType.getSelectedItem().toString();
        binding.btnUpload.setEnabled(false);
        binding.btnUpload.setText("Uploading...");

        try (InputStream is = getContentResolver().openInputStream(selectedFileUri)) {
            byte[] fileBytes = new byte[is.available()];
            is.read(fileBytes);

            RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(selectedFileUri)), fileBytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", selectedFileUri.getLastPathSegment(), requestFile);
            RequestBody type = RequestBody.create(MultipartBody.FORM, docType);

            RetrofitClient.get().uploadDocument(tokenManager.getBearerToken(), type, body)
                    .enqueue(new Callback<com.workforcex.worker.api.Document>() {
                        @Override
                        public void onResponse(Call<com.workforcex.worker.api.Document> call, Response<com.workforcex.worker.api.Document> response) {
                            binding.btnUpload.setEnabled(true);
                            binding.btnUpload.setText("Upload");
                            if (response.isSuccessful()) {
                                Toast.makeText(VerificationActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                                loadVerificationStatus(); // Refresh the list
                                binding.tvSelectedFile.setText("No file selected");
                                selectedFileUri = null;
                            } else {
                                Toast.makeText(VerificationActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<com.workforcex.worker.api.Document> call, Throwable t) {
                            binding.btnUpload.setEnabled(true);
                            binding.btnUpload.setText("Upload");
                            Toast.makeText(VerificationActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            binding.btnUpload.setEnabled(true);
            binding.btnUpload.setText("Upload");
            Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show();
        }
    }
}
