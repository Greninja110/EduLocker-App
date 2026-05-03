package com.example.edulocker.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.edulocker.adapters.RequiredDocStatusAdapter;
import com.example.edulocker.databinding.ActivityScholarshipApplicationBinding;
import com.example.edulocker.models.EduDocument;
import com.example.edulocker.models.RequiredDoc;
import com.example.edulocker.models.Scholarship;
import com.example.edulocker.models.ScholarshipApplication;
import com.example.edulocker.models.Student;
import com.example.edulocker.repositories.DocumentRepository;
import com.example.edulocker.repositories.ScholarshipRepository;
import com.example.edulocker.repositories.StudentRepository;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class ScholarshipApplicationActivity extends AppCompatActivity {

    private ActivityScholarshipApplicationBinding binding;
    private SessionManager session;
    private Student currentStudent;
    private Scholarship currentScholarship;
    private List<EduDocument> studentDocs = new ArrayList<>();
    private List<String> attachedDocIds = new ArrayList<>();
    private boolean hasMissingMandatory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScholarshipApplicationBinding.inflate(getLayoutInflater());
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

        String scholarshipId = getIntent().getStringExtra(Constants.EXTRA_SCHOLARSHIP_ID);
        loadScholarship(scholarshipId);

        // Virtual ID entry — student types/pastes passport ID to auto-fill
        binding.btnVerifyId.setOnClickListener(v -> {
            String enteredId = binding.etPassportIdEntry.getText().toString().trim().toUpperCase();
            if (enteredId.isEmpty()) {
                binding.etPassportIdEntry.setError("Enter your Passport ID");
                return;
            }
            loadStudentByPassportId(enteredId);
        });

        binding.btnSubmit.setOnClickListener(v -> submitApplication());
    }

    private void loadScholarship(String scholarshipId) {
        Constants.db().collection(Constants.COL_SCHOLARSHIPS)
                .document(scholarshipId).get()
                .addOnSuccessListener(doc -> {
                    currentScholarship = doc.toObject(Scholarship.class);
                    runOnUiThread(() -> {
                        if (currentScholarship != null) {
                            binding.tvScholarshipTitle.setText(currentScholarship.getTitle());
                            binding.tvIssuedBy.setText("Issued by: " + currentScholarship.getIssuedBy());
                            binding.tvDeadline.setText("Last Date: " + currentScholarship.getDeadline());
                            binding.tvAmount.setText("Amount: " + currentScholarship.getAmount());
                            String cat = currentScholarship.getEligibleCategory();
                            String cls = currentScholarship.getEligibleClass();
                            binding.tvEligibility.setText("Eligible: " + cat + " | " + cls);
                        }
                    });
                });
    }

    private void loadStudentByPassportId(String passportId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnVerifyId.setEnabled(false);

        new StudentRepository().getStudent(passportId, new StudentRepository.StudentCallback() {
            @Override
            public void onSuccess(Student student) {
                currentStudent = student;
                runOnUiThread(() -> populateStudentInfo(student));
                checkAlreadyApplied(passportId);
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnVerifyId.setEnabled(true);
                    Toast.makeText(ScholarshipApplicationActivity.this,
                            "Student not found. Check your Passport ID.", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void checkAlreadyApplied(String passportId) {
        Constants.db().collection(Constants.COL_APPLICATIONS)
                .whereEqualTo("passportId", passportId)
                .whereEqualTo("scholarshipId", currentScholarship.getScholarshipId())
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        // Already applied — show status, block submit
                        ScholarshipApplication existing =
                                query.getDocuments().get(0).toObject(ScholarshipApplication.class);
                        runOnUiThread(() -> {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnVerifyId.setEnabled(true);
                            binding.layoutDocStatus.setVisibility(View.VISIBLE);
                            binding.rvDocStatus.setAdapter(null);
                            String status = existing != null ? existing.getStatus() : "Submitted";
                            binding.tvDocStatusNote.setText(
                                    "You have already applied for this scholarship.\nStatus: " + status);
                            binding.tvDocStatusNote.setVisibility(View.VISIBLE);
                            binding.btnSubmit.setEnabled(false);
                            binding.btnSubmit.setText("Already Applied");
                        });
                    } else {
                        // Not applied yet — load docs and continue
                        String viewerUid  = session.getUserUid();
                        String viewerRole = session.getUserRole();
                        new DocumentRepository().getDocumentsForStudentWithPermission(
                                passportId, viewerUid, viewerRole,
                                new DocumentRepository.DocumentListCallback() {
                                    @Override
                                    public void onSuccess(List<EduDocument> docs) {
                                        studentDocs = docs;
                                        runOnUiThread(() -> {
                                            binding.progressBar.setVisibility(View.GONE);
                                            binding.btnVerifyId.setEnabled(true);
                                            showDocumentStatus();
                                        });
                                    }
                                    @Override
                                    public void onFailure(String error) {
                                        runOnUiThread(() -> {
                                            binding.progressBar.setVisibility(View.GONE);
                                            binding.btnVerifyId.setEnabled(true);
                                            Toast.makeText(ScholarshipApplicationActivity.this,
                                                    "Could not load documents", Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // If the duplicate check itself fails, still allow proceeding
                    String viewerUid  = session.getUserUid();
                    String viewerRole = session.getUserRole();
                    new DocumentRepository().getDocumentsForStudentWithPermission(
                            passportId, viewerUid, viewerRole,
                            new DocumentRepository.DocumentListCallback() {
                                @Override
                                public void onSuccess(List<EduDocument> docs) {
                                    studentDocs = docs;
                                    runOnUiThread(() -> {
                                        binding.progressBar.setVisibility(View.GONE);
                                        binding.btnVerifyId.setEnabled(true);
                                        showDocumentStatus();
                                    });
                                }
                                @Override
                                public void onFailure(String error) {
                                    runOnUiThread(() -> {
                                        binding.progressBar.setVisibility(View.GONE);
                                        binding.btnVerifyId.setEnabled(true);
                                    });
                                }
                            });
                });
    }

    private void populateStudentInfo(Student student) {
        binding.layoutStudentInfo.setVisibility(View.VISIBLE);
        binding.tvStudentName.setText(student.getName());
        binding.tvPassportId.setText(student.getPassportId());
        binding.tvDob.setText(student.getDob());
        binding.tvFatherName.setText(student.getFatherName());
        binding.tvClass.setText(student.getStudentClass());
        binding.tvCategory.setText(student.getCategory());
        binding.tvAddress.setText(student.getAddress());
    }

    private void showDocumentStatus() {
        binding.layoutDocStatus.setVisibility(View.VISIBLE);
        attachedDocIds.clear();
        hasMissingMandatory = false;

        List<RequiredDoc> requiredDocs = currentScholarship != null
                && currentScholarship.getRequiredDocs() != null
                ? currentScholarship.getRequiredDocs() : new ArrayList<>();

        // Build doc status list
        List<RequiredDocStatusAdapter.DocStatus> statusList = new ArrayList<>();

        for (RequiredDoc rd : requiredDocs) {
            EduDocument match = findMatchingDoc(rd.getDocTypeName());
            RequiredDocStatusAdapter.DocStatus status = new RequiredDocStatusAdapter.DocStatus();
            status.docTypeName = rd.getDocTypeName();
            status.mandatory = rd.isMandatory();
            status.document = match;
            statusList.add(status);

            if (match != null) {
                attachedDocIds.add(match.getDocId());
            } else if (rd.isMandatory()) {
                hasMissingMandatory = true;
            }
        }

        // Also attach all other docs the student has (beyond required list)
        for (EduDocument doc : studentDocs) {
            boolean alreadyAdded = attachedDocIds.contains(doc.getDocId());
            if (!alreadyAdded) attachedDocIds.add(doc.getDocId());
        }

        binding.rvDocStatus.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDocStatus.setAdapter(new RequiredDocStatusAdapter(statusList, this));

        if (hasMissingMandatory) {
            binding.tvDocStatusNote.setText("⚠ Upload all mandatory (*) documents before submitting.");
            binding.tvDocStatusNote.setVisibility(View.VISIBLE);
            binding.btnSubmit.setEnabled(false);
        } else {
            binding.tvDocStatusNote.setText("All mandatory documents found. Ready to submit.");
            binding.tvDocStatusNote.setVisibility(View.VISIBLE);
            binding.btnSubmit.setEnabled(true);
        }

        if (requiredDocs.isEmpty()) {
            binding.tvDocStatusNote.setText(studentDocs.size() + " document(s) will be attached.");
            binding.btnSubmit.setEnabled(true);
        }
    }

    private EduDocument findMatchingDoc(String docTypeName) {
        if (docTypeName == null) return null;
        for (EduDocument doc : studentDocs) {
            if (docTypeName.equalsIgnoreCase(doc.getType())) return doc;
        }
        return null;
    }

    private void submitApplication() {
        if (currentStudent == null || currentScholarship == null) {
            Toast.makeText(this, "Verify your Passport ID first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (hasMissingMandatory) {
            Toast.makeText(this, "Upload all mandatory documents first", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);
        // Final duplicate guard before writing
        Constants.db().collection(Constants.COL_APPLICATIONS)
                .whereEqualTo("passportId", currentStudent.getPassportId())
                .whereEqualTo("scholarshipId", currentScholarship.getScholarshipId())
                .limit(1).get()
                .addOnSuccessListener(q -> {
                    if (!q.isEmpty()) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            binding.btnSubmit.setEnabled(false);
                            binding.btnSubmit.setText("Already Applied");
                            Toast.makeText(this,
                                    "You have already applied for this scholarship.",
                                    Toast.LENGTH_LONG).show();
                        });
                    } else {
                        doSubmit();
                    }
                })
                .addOnFailureListener(e -> doSubmit()); // network issue — let the write proceed
    }

    private void doSubmit() {

        ScholarshipApplication application = new ScholarshipApplication();
        application.setPassportId(currentStudent.getPassportId());
        application.setStudentName(currentStudent.getName());
        application.setStudentClass(currentStudent.getStudentClass());
        application.setStudentCategory(currentStudent.getCategory());
        application.setSchoolId(currentStudent.getSchoolId());
        application.setScholarshipId(currentScholarship.getScholarshipId());
        application.setScholarshipTitle(currentScholarship.getTitle());
        application.setAttachedDocIds(new ArrayList<>(attachedDocIds));

        new ScholarshipRepository().submitApplication(application,
                new ScholarshipRepository.ApplicationCallback() {
                    @Override
                    public void onSuccess(String applicationId) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            new androidx.appcompat.app.AlertDialog.Builder(ScholarshipApplicationActivity.this)
                                    .setTitle("Application Submitted!")
                                    .setMessage("Your application for \""
                                            + currentScholarship.getTitle()
                                            + "\" has been submitted.\n\nApplication ID: "
                                            + applicationId.substring(0, 8).toUpperCase())
                                    .setPositiveButton("Done", (d, w) -> finish())
                                    .setCancelable(false)
                                    .show();
                        });
                    }
                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(ScholarshipApplicationActivity.this,
                                    "Submission failed: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private void setLoading(boolean loading) {
        binding.btnSubmit.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
