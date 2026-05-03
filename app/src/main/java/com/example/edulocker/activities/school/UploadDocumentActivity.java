package com.example.edulocker.activities.school;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.edulocker.databinding.ActivityUploadDocumentBinding;
import com.example.edulocker.models.DocumentType;
import com.example.edulocker.models.EduDocument;
import com.example.edulocker.models.Student;
import com.example.edulocker.models.User;
import com.example.edulocker.repositories.DocumentRepository;
import com.example.edulocker.repositories.DocumentTypeRepository;
import com.example.edulocker.repositories.StudentRepository;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class UploadDocumentActivity extends AppCompatActivity {

    private ActivityUploadDocumentBinding binding;
    private Uri selectedFileUri;
    private String selectedPassportId;
    private List<Student> loadedStudents = new ArrayList<>();
    private List<DocumentType> docTypes = new ArrayList<>();
    private boolean showingOtherField = false;

    private final ActivityResultLauncher<String[]> filePicker =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    binding.tvFileName.setText(uri.getLastPathSegment());
                    binding.tvFileName.setVisibility(View.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadDocumentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, top, 0, 0);
            return insets;
        });
        binding.btnBack.setOnClickListener(v -> finish());

        loadDocumentTypes();

        // Student picker — pre-filled (locked) or selectable dropdown
        String prefilledId = getIntent().getStringExtra(Constants.EXTRA_PASSPORT_ID);
        if (prefilledId != null) {
            selectedPassportId = prefilledId;
            binding.spinnerStudent.setText(prefilledId);
            binding.spinnerStudent.setEnabled(false);
            binding.layoutStudentPicker.setHint("Student");
        } else {
            loadStudentsForPicker();
        }

        binding.btnPickFile.setOnClickListener(v ->
                filePicker.launch(new String[]{"application/pdf", "image/*"}));
        binding.btnUpload.setOnClickListener(v -> upload());
    }

    private void loadDocumentTypes() {
        new DocumentTypeRepository().getDocumentTypes(new DocumentTypeRepository.DocTypeListCallback() {
            @Override
            public void onSuccess(List<DocumentType> types) {
                docTypes = types;
                List<String> names = new ArrayList<>();
                for (DocumentType dt : types) names.add(dt.getName());
                names.add("Other");
                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            UploadDocumentActivity.this,
                            android.R.layout.simple_dropdown_item_1line, names);
                    binding.spinnerDocType.setAdapter(adapter);
                    binding.spinnerDocType.setOnClickListener(v -> binding.spinnerDocType.showDropDown());
                    binding.spinnerDocType.setOnItemClickListener((parent, view, pos, id) -> {
                        boolean isOther = pos == names.size() - 1;
                        binding.tilOtherDocTitle.setVisibility(isOther ? View.VISIBLE : View.GONE);
                        showingOtherField = isOther;
                    });
                });
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(UploadDocumentActivity.this,
                        "Could not load doc types: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStudentsForPicker() {
        String schoolId = new SessionManager(this).getSchoolId();
        if (schoolId == null) return;
        new StudentRepository().getStudentsBySchool(schoolId,
                new StudentRepository.StudentListCallback() {
                    @Override
                    public void onSuccess(List<Student> students) {
                        loadedStudents = students;
                        List<String> displayItems = new ArrayList<>();
                        for (Student s : students)
                            displayItems.add(s.getName() + "  (" + s.getPassportId() + ")");
                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    UploadDocumentActivity.this,
                                    android.R.layout.simple_dropdown_item_1line, displayItems);
                            binding.spinnerStudent.setAdapter(adapter);
                            binding.spinnerStudent.setOnClickListener(
                                    v -> binding.spinnerStudent.showDropDown());
                            binding.spinnerStudent.setOnItemClickListener((parent, view, pos, id) ->
                                    selectedPassportId = loadedStudents.get(pos).getPassportId());
                        });
                    }
                    @Override
                    public void onFailure(String error) {}
                });
    }

    private void upload() {
        if (selectedPassportId == null || selectedPassportId.isEmpty()) {
            Toast.makeText(this, "Select a student first", Toast.LENGTH_SHORT).show();
            return;
        }
        String docType = binding.spinnerDocType.getText().toString().trim();
        String year    = binding.etAcademicYear.getText().toString().trim();

        if (docType.isEmpty()) {
            Toast.makeText(this, "Select document type", Toast.LENGTH_SHORT).show();
            return;
        }

        String otherTitle = null;
        if (showingOtherField) {
            otherTitle = binding.etOtherDocTitle.getText().toString().trim();
            if (otherTitle.isEmpty()) {
                binding.etOtherDocTitle.setError("Enter a name for this document");
                return;
            }
        }

        // Use docType name as the title for canonical types
        String title = "Other".equals(docType) ? otherTitle : docType;

        if (selectedFileUri == null) {
            Toast.makeText(this, "Pick a file first", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        SessionManager session = new SessionManager(this);
        String role = session.getUserRole();

        EduDocument doc = new EduDocument();
        doc.setPassportId(selectedPassportId);
        doc.setType(docType);
        doc.setOtherTitle(otherTitle);
        doc.setTitle(title);
        doc.setAcademicYear(year);
        doc.setUploadedByUserId(session.getUserUid());
        doc.setUploadedByRole(role);
        doc.setVerified(true);
        doc.setTimestamp(Timestamp.now());
        // Set visibility: parent uploads = private, everyone else = shared
        doc.setVisibility(User.ROLE_PARENT.equals(role)
                ? Constants.VISIBILITY_PRIVATE : Constants.VISIBILITY_SHARED);

        new DocumentRepository().uploadDocument(this, selectedFileUri, doc,
                new DocumentRepository.UploadProgressCallback() {
                    @Override
                    public void onProgress(int percent) {
                        runOnUiThread(() -> {
                            binding.progressBar.setVisibility(View.VISIBLE);
                            binding.tvUploadProgress.setVisibility(View.VISIBLE);
                            binding.tvUploadProgress.setText("Uploading... " + percent + "%");
                        });
                    }
                    @Override
                    public void onSuccess(String downloadUrl) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(UploadDocumentActivity.this,
                                    "Document uploaded!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(UploadDocumentActivity.this,
                                    "Upload failed: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private void setLoading(boolean loading) {
        binding.btnUpload.setEnabled(!loading);
        if (!loading) {
            binding.progressBar.setVisibility(View.GONE);
            binding.tvUploadProgress.setVisibility(View.GONE);
        }
    }
}
