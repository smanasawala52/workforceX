package com.workforcex.worker.ui.worker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.workforcex.shared.RetrofitClient
import com.workforcex.shared.models.Document
import com.workforcex.shared.models.Verification
import com.workforcex.worker.databinding.ActivityVerificationBinding
import com.workforcex.worker.utils.TokenManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream

class VerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationBinding
    private lateinit var tokenManager: TokenManager
    private var selectedFileUri: Uri? = null

    private val filePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedFileUri = uri
                binding.tvSelectedFile.text = "Selected: " + uri.lastPathSegment
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Verification Center"
        tokenManager = TokenManager(this)

        binding.rvVerificationStatus.layoutManager = LinearLayoutManager(this)
        binding.rvDocuments.layoutManager = LinearLayoutManager(this)
        setupDocumentSpinner()
        loadVerificationStatus()
        loadMyDocuments()

        binding.btnChooseFile.setOnClickListener {
            if (binding.spinnerDocumentType.selectedItem != null) {
                filePicker.launch("*/*")
            } else {
                Toast.makeText(this, "Please select a document type first", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnUpload.setOnClickListener { uploadDocument() }
    }

    private fun setupDocumentSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            arrayOf("AADHAAR", "PAN", "DRIVING_LICENSE", "PASSPORT", "CERTIFICATION", "PHOTO", "INTRO_VIDEO")
        )
        binding.spinnerDocumentType.adapter = adapter
    }

    private fun loadVerificationStatus() {
        binding.progressBar.visibility = View.VISIBLE
        RetrofitClient.get().getVerificationStatus(tokenManager.getBearerToken())
            .enqueue(object : Callback<List<Verification>> {
                override fun onResponse(
                    call: Call<List<Verification>>,
                    response: Response<List<Verification>>
                ) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        binding.rvVerificationStatus.adapter = VerificationStatusAdapter(response.body()!!)
                    }
                }

                override fun onFailure(call: Call<List<Verification>>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                }
            })
    }

    private fun loadMyDocuments() {
        RetrofitClient.get().getMyDocuments(tokenManager.getBearerToken())
            .enqueue(object : Callback<List<Document>> {
                override fun onResponse(
                    call: Call<List<Document>>,
                    response: Response<List<Document>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        binding.rvDocuments.adapter = DocumentListAdapter(response.body()!!) { doc ->
                            openDocument(doc)
                        }
                    }
                }

                override fun onFailure(call: Call<List<Document>>, t: Throwable) {
                    // Non-critical: verification status above still loads fine.
                }
            })
    }

    private fun openDocument(document: Document) {
        val fullUrl = if (document.fileUrl.startsWith("http")) {
            document.fileUrl
        } else {
            RetrofitClient.baseUrl().trimEnd('/') + document.fileUrl
        }
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)))
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open document", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadDocument() {
        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file to upload", Toast.LENGTH_SHORT).show()
            return
        }

        val docType = binding.spinnerDocumentType.selectedItem.toString()
        binding.btnUpload.isEnabled = false
        binding.btnUpload.text = "Uploading..."

        try {
            val `is` = contentResolver.openInputStream(selectedFileUri!!)
            val fileBytes = ByteArray(`is`!!.available())
            `is`.read(fileBytes)
            `is`.close()

            val requestFile = fileBytes.toRequestBody(contentResolver.getType(selectedFileUri!!)?.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", selectedFileUri!!.lastPathSegment, requestFile)
            val type = docType.toRequestBody(MultipartBody.FORM)

            RetrofitClient.get().uploadDocument(tokenManager.getBearerToken(), type, body)
                .enqueue(object : Callback<com.workforcex.shared.models.Document> {
                    override fun onResponse(
                        call: Call<com.workforcex.shared.models.Document>,
                        response: Response<com.workforcex.shared.models.Document>
                    ) {
                        binding.btnUpload.isEnabled = true
                        binding.btnUpload.text = "Upload"
                        if (response.isSuccessful) {
                            Toast.makeText(this@VerificationActivity, "Upload successful", Toast.LENGTH_SHORT).show()
                            loadVerificationStatus() // Refresh the list
                            loadMyDocuments() // Refresh the list of uploaded files too
                            binding.tvSelectedFile.text = "No file selected"
                            selectedFileUri = null
                        } else {
                            Toast.makeText(this@VerificationActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<com.workforcex.shared.models.Document>, t: Throwable) {
                        binding.btnUpload.isEnabled = true
                        binding.btnUpload.text = "Upload"
                        Toast.makeText(this@VerificationActivity, "Network error", Toast.LENGTH_SHORT).show()
                    }
                })
        } catch (e: Exception) {
            binding.btnUpload.isEnabled = true
            binding.btnUpload.text = "Upload"
            Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show()
        }
    }
}