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
import com.workforcex.worker.api.Document;
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
    }

    private void setupDocumentSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"AADHAAR", "PAN", "DRIVING_LICENSE", "PASSPORT", "CERTIFICATION", "PHOTO", "INTRO_VIDEO"});
        binding.spinnerDocumentType.setAdapter(adapter);
    }

    private void loadVerificationStatus() {
        RetrofitClient.get().getVerificationStatus(tokenManager.getBearerToken())
                .enqueue(new Callback<List<Verification>>() {
                    @Override
                    public void onResponse(Call<List<Verification>> call, Response<List<Verification>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            binding.rvVerificationStatus.setAdapter(new VerificationStatusAdapter(response.body()));
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Verification>> call, Throwable t) {}
                });
    }
}
