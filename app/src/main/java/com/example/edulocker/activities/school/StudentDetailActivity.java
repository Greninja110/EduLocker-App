package com.example.edulocker.activities.school;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.edulocker.adapters.DocumentAdapter;
import com.example.edulocker.databinding.ActivityStudentDetailBinding;
import com.example.edulocker.models.EduDocument;
import com.example.edulocker.models.Student;
import com.example.edulocker.repositories.DocumentRepository;
import com.example.edulocker.repositories.StudentRepository;
import com.example.edulocker.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class StudentDetailActivity extends AppCompatActivity
        implements DocumentAdapter.DocumentClickListener {

    private ActivityStudentDetailBinding binding;
    private String passportId;
    private final List<EduDocument> docList = new ArrayList<>();
    private DocumentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, statusBarHeight, 0, 0);
            return insets;
        });

        binding.btnBack.setOnClickListener(v -> finish());

        passportId = getIntent().getStringExtra(Constants.EXTRA_PASSPORT_ID);
        if (passportId != null) {
            loadStudent();
            loadDocuments();
        }

        binding.btnUploadDoc.setOnClickListener(v -> {
            Intent intent = new Intent(this, UploadDocumentActivity.class);
            intent.putExtra(Constants.EXTRA_PASSPORT_ID, passportId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDocuments();
    }

    private void loadStudent() {
        new StudentRepository().getStudent(passportId, new StudentRepository.StudentCallback() {
            @Override public void onSuccess(Student student) {
                runOnUiThread(() -> {
                    binding.tvToolbarTitle.setText(student.getName());
                    binding.tvStudentName.setText(student.getName());
                    binding.tvPassportId.setText(student.getPassportId());
                    binding.tvClass.setText(student.getStudentClass() + " - " + student.getSection());
                    binding.tvFather.setText("Father: " + student.getFatherName());
                    binding.tvPhone.setText("Phone: " + student.getContactNumber());
                    binding.tvKyc.setText("KYC: " + student.getKycStatus());
                });
            }
            @Override public void onFailure(String error) {}
        });
    }

    private void loadDocuments() {
        new DocumentRepository().getDocumentsForStudent(passportId,
                new DocumentRepository.DocumentListCallback() {
                    @Override public void onSuccess(List<EduDocument> documents) {
                        runOnUiThread(() -> {
                            docList.clear();
                            docList.addAll(documents);
                            binding.rvDocuments.setLayoutManager(
                                    new LinearLayoutManager(StudentDetailActivity.this));
                            adapter = new DocumentAdapter(docList, StudentDetailActivity.this,
                                    (doc, pos) -> confirmDelete(doc, pos), null);
                            binding.rvDocuments.setAdapter(adapter);
                            binding.tvDocCount.setText(documents.size() + " document(s)");
                        });
                    }
                    @Override public void onFailure(String error) {}
                });
    }

    private void confirmDelete(EduDocument doc, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Document")
                .setMessage("Delete \"" + doc.getTitle() + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    new DocumentRepository().deleteDocument(doc.getDocId(),
                            new DocumentRepository.DocumentDeleteCallback() {
                                @Override public void onSuccess() {
                                    runOnUiThread(() -> {
                                        if (adapter != null) adapter.removeAt(position);
                                        binding.tvDocCount.setText(docList.size() + " document(s)");
                                    });
                                }
                                @Override public void onFailure(String error) {
                                    runOnUiThread(() -> Toast.makeText(StudentDetailActivity.this,
                                            "Delete failed: " + error, Toast.LENGTH_SHORT).show());
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDocumentClick(EduDocument document) {
        if (document.getFileUrl() != null && !document.getFileUrl().isEmpty())
            startActivity(new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse(document.getFileUrl())));
    }
}
