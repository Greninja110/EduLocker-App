package com.example.edulocker.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.edulocker.adapters.ApplicationReviewAdapter;
import com.example.edulocker.databinding.ActivityApplicationReviewBinding;
import com.example.edulocker.models.ScholarshipApplication;
import com.example.edulocker.models.User;
import com.example.edulocker.repositories.ScholarshipRepository;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ApplicationReviewActivity extends AppCompatActivity {

    private ActivityApplicationReviewBinding binding;
    private SessionManager session;
    private ApplicationReviewAdapter adapter;
    private final List<ScholarshipApplication> applications = new ArrayList<>();
    private ScholarshipRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityApplicationReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, top, 0, 0);
            return insets;
        });
        binding.btnBack.setOnClickListener(v -> finish());
        session = new SessionManager(this);
        repo = new ScholarshipRepository();

        String scholarshipId    = getIntent().getStringExtra(Constants.EXTRA_SCHOLARSHIP_ID);
        String scholarshipTitle = getIntent().getStringExtra(Constants.EXTRA_SCHOLARSHIP_TITLE);

        binding.tvTitle.setText(scholarshipTitle != null ? scholarshipTitle : "Applications");

        adapter = new ApplicationReviewAdapter(applications, this::onApprove, this::onReject);
        binding.rvApplications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvApplications.setAdapter(adapter);

        loadApplications(scholarshipId);
    }

    private void loadApplications(String scholarshipId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        String role = session.getUserRole();

        if (User.ROLE_GOVERNMENT.equals(role)) {
            repo.getApplicationsForScholarship(scholarshipId,
                    new ScholarshipRepository.ApplicationListCallback() {
                        @Override public void onSuccess(List<ScholarshipApplication> list) { show(list); }
                        @Override public void onFailure(String error) { showError(error); }
                    });
        } else {
            repo.getApplicationsForSchoolAndScholarship(session.getSchoolId(), scholarshipId,
                    new ScholarshipRepository.ApplicationListCallback() {
                        @Override public void onSuccess(List<ScholarshipApplication> list) { show(list); }
                        @Override public void onFailure(String error) { showError(error); }
                    });
        }
    }

    private void show(List<ScholarshipApplication> list) {
        runOnUiThread(() -> {
            binding.progressBar.setVisibility(View.GONE);
            applications.clear();
            applications.addAll(list);
            adapter.notifyDataSetChanged();
            binding.tvEmpty.setVisibility(applications.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void showError(String error) {
        runOnUiThread(() -> {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    private void onApprove(ScholarshipApplication app) {
        showReviewDialog(app, true);
    }

    private void onReject(ScholarshipApplication app) {
        showReviewDialog(app, false);
    }

    private void showReviewDialog(ScholarshipApplication app, boolean approve) {
        View v = getLayoutInflater().inflate(
                com.example.edulocker.R.layout.dialog_review_note, null);
        com.google.android.material.textfield.TextInputEditText etNote =
                v.findViewById(com.example.edulocker.R.id.et_review_note);

        new AlertDialog.Builder(this)
                .setTitle(approve ? "Approve Application" : "Reject Application")
                .setMessage("Student: " + app.getStudentName())
                .setView(v)
                .setPositiveButton(approve ? "Approve" : "Reject", (dialog, w) -> {
                    String note = etNote.getText() != null
                            ? etNote.getText().toString().trim() : "";
                    String status = approve
                            ? ScholarshipApplication.STATUS_APPROVED
                            : ScholarshipApplication.STATUS_REJECTED;
                    binding.progressBar.setVisibility(View.VISIBLE);
                    repo.updateApplicationStatus(app.getApplicationId(), status, note,
                            session.getUserUid(),
                            new ScholarshipRepository.SimpleCallback() {
                                @Override public void onSuccess() {
                                    runOnUiThread(() -> {
                                        binding.progressBar.setVisibility(View.GONE);
                                        app.setStatus(status);
                                        app.setReviewNote(note);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(ApplicationReviewActivity.this,
                                                approve ? "Approved!" : "Rejected.",
                                                Toast.LENGTH_SHORT).show();
                                    });
                                }
                                @Override public void onFailure(String error) {
                                    runOnUiThread(() -> {
                                        binding.progressBar.setVisibility(View.GONE);
                                        Toast.makeText(ApplicationReviewActivity.this,
                                                "Error: " + error, Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
