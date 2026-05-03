package com.example.edulocker.activities.school;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.edulocker.databinding.ActivityPostNoticeBinding;
import com.example.edulocker.models.ClassAssignment;
import com.example.edulocker.models.DocumentType;
import com.example.edulocker.models.Notice;
import com.example.edulocker.models.RequiredDoc;
import com.example.edulocker.models.Scholarship;
import com.example.edulocker.models.User;
import com.example.edulocker.repositories.DocumentTypeRepository;
import com.example.edulocker.repositories.NoticeRepository;
import com.example.edulocker.repositories.ScholarshipRepository;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class PostNoticeActivity extends AppCompatActivity {

    public static final String EXTRA_SCHOOL_ID    = "extra_school_id";
    public static final String EXTRA_NOTICE_TYPE  = "extra_notice_type";
    public static final String EXTRA_NOTICE_ID    = "extra_notice_id";
    public static final String EXTRA_NOTICE_TITLE = "extra_notice_title";
    public static final String EXTRA_NOTICE_BODY  = "extra_notice_body";
    public static final String EXTRA_NOTICE_STATE = "extra_notice_state";

    private static final String POST_TYPE_NOTICE      = "Notice/Announcement";
    private static final String POST_TYPE_SCHOLARSHIP = "Scholarship";

    private ActivityPostNoticeBinding binding;
    private SessionManager session;
    private boolean isStateNotice;
    private String editingNoticeId;
    private final List<ClassAssignment> teacherClasses = new ArrayList<>();
    private List<DocumentType> availableDocTypes = new ArrayList<>();
    private final List<RequiredDoc> selectedRequiredDocs = new ArrayList<>();
    private String currentPostType = POST_TYPE_NOTICE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostNoticeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, statusBarHeight, 0, 0);
            return insets;
        });
        binding.btnBack.setOnClickListener(v -> finish());
        session = new SessionManager(this);

        editingNoticeId = getIntent().getStringExtra(EXTRA_NOTICE_ID);
        boolean isEditing = editingNoticeId != null;

        isStateNotice = Notice.TYPE_STATE.equals(getIntent().getStringExtra(EXTRA_NOTICE_TYPE))
                || User.ROLE_GOVERNMENT.equals(session.getUserRole());

        setupPostTypeDropdown();
        setupRoleSpecificUI();
        setupScholarshipFields();

        if (isEditing) {
            binding.tvToolbarTitle.setText(isStateNotice ? "Edit State Notice" : "Edit Notice");
            binding.btnPost.setText("Update");
            binding.etNoticeTitle.setText(getIntent().getStringExtra(EXTRA_NOTICE_TITLE));
            binding.etNoticeContent.setText(getIntent().getStringExtra(EXTRA_NOTICE_BODY));
            // Editing only supports notice type for now
            binding.tilPostType.setVisibility(View.GONE);
        } else {
            binding.tvToolbarTitle.setText(isStateNotice ? "Post State Notice" : "Post");
        }

        binding.btnPost.setOnClickListener(v -> {
            if (isEditing) updateNotice();
            else if (POST_TYPE_SCHOLARSHIP.equals(currentPostType)) postScholarship();
            else postNotice();
        });
    }

    private void setupPostTypeDropdown() {
        String role = session.getUserRole();
        // Teachers can only post notices, not scholarships
        if (User.ROLE_TEACHER.equals(role)) {
            binding.tilPostType.setVisibility(View.GONE);
            return;
        }
        List<String> postTypes = Arrays.asList(POST_TYPE_NOTICE, POST_TYPE_SCHOLARSHIP);
        binding.spinnerPostType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, postTypes));
        binding.spinnerPostType.setText(POST_TYPE_NOTICE, false);
        binding.spinnerPostType.setOnClickListener(v -> binding.spinnerPostType.showDropDown());
        binding.spinnerPostType.setOnItemClickListener((parent, view, pos, id) -> {
            currentPostType = postTypes.get(pos);
            applyPostType();
        });
    }

    private void applyPostType() {
        boolean isScholarship = POST_TYPE_SCHOLARSHIP.equals(currentPostType);
        binding.layoutScholarshipFields.setVisibility(isScholarship ? View.VISIBLE : View.GONE);
        binding.tilNoticeContent.setVisibility(isScholarship ? View.GONE : View.VISIBLE);
        binding.tvScholarshipDescLabel.setVisibility(isScholarship ? View.VISIBLE : View.GONE);
        binding.btnPost.setText(isScholarship ? "Post Scholarship" : "Post Notice");
        if (isScholarship && availableDocTypes.isEmpty()) loadDocumentTypes();
    }

    private void setupRoleSpecificUI() {
        String role = session.getUserRole();

        // Teacher: show class selector
        if (User.ROLE_TEACHER.equals(role)) {
            binding.tilClass.setVisibility(View.VISIBLE);
            loadTeacherClasses();
        }

        // School admin: show audience selector
        if (User.ROLE_SCHOOL.equals(role)) {
            binding.tilAudience.setVisibility(View.VISIBLE);
            String[] audiences = {"All", "Students Only", "Faculty Only"};
            binding.spinnerAudience.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, audiences));
            binding.spinnerAudience.setText("All", false);
            binding.spinnerAudience.setOnClickListener(v -> binding.spinnerAudience.showDropDown());
        }

        // Gov: show state selector
        if (isStateNotice) {
            binding.tilState.setVisibility(View.VISIBLE);
            List<String> stateOptions = new ArrayList<>();
            stateOptions.add("All India");
            stateOptions.addAll(Arrays.asList(Constants.STATE_NAMES));
            binding.spinnerState.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, stateOptions));
            binding.spinnerState.setOnClickListener(v -> binding.spinnerState.showDropDown());
            String existingState = getIntent().getStringExtra(EXTRA_NOTICE_STATE);
            binding.spinnerState.setText(
                    existingState == null || existingState.isEmpty() || "All".equals(existingState)
                            ? "All India" : existingState, false);
        }
    }

    private void setupScholarshipFields() {
        // Category dropdown
        String[] categories = {"All", "SC/ST", "OBC", "EWS", "General"};
        binding.spinnerCategory.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories));
        binding.spinnerCategory.setText("All", false);
        binding.spinnerCategory.setOnClickListener(v -> binding.spinnerCategory.showDropDown());

        // Class dropdown
        String[] classes = {"All", "Class 1", "Class 2", "Class 3", "Class 4", "Class 5",
                "Class 6", "Class 7", "Class 8", "Class 9", "Class 10",
                "Class 11", "Class 12", "Higher Education"};
        binding.spinnerEligibleClass.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, classes));
        binding.spinnerEligibleClass.setText("All", false);
        binding.spinnerEligibleClass.setOnClickListener(v -> binding.spinnerEligibleClass.showDropDown());

        // Deadline date picker
        binding.etDeadline.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                String date = String.format("%02d/%02d/%04d", day, month + 1, year);
                binding.etDeadline.setText(date);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Required docs — shown as a scrollable chip list populated after loading
        binding.btnAddRequiredDoc.setOnClickListener(v -> showAddRequiredDocDialog());
    }

    private void loadDocumentTypes() {
        new DocumentTypeRepository().getDocumentTypes(new DocumentTypeRepository.DocTypeListCallback() {
            @Override
            public void onSuccess(List<DocumentType> types) {
                availableDocTypes = types;
            }
            @Override
            public void onFailure(String error) {}
        });
    }

    private void showAddRequiredDocDialog() {
        if (availableDocTypes.isEmpty()) {
            Toast.makeText(this, "Loading document types…", Toast.LENGTH_SHORT).show();
            loadDocumentTypes();
            return;
        }
        String[] names = new String[availableDocTypes.size()];
        boolean[] checked = new boolean[availableDocTypes.size()];
        for (int i = 0; i < availableDocTypes.size(); i++) {
            names[i] = availableDocTypes.get(i).getName();
            for (RequiredDoc rd : selectedRequiredDocs) {
                if (rd.getDocTypeName().equals(names[i])) { checked[i] = true; break; }
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Required Documents")
                .setMultiChoiceItems(names, checked, (dialog, which, isChecked) ->
                        checked[which] = isChecked)
                .setPositiveButton("Done", (dialog, w) -> {
                    selectedRequiredDocs.clear();
                    // For each selected doc, ask if it's mandatory or optional
                    List<Integer> selected = new ArrayList<>();
                    for (int i = 0; i < checked.length; i++) if (checked[i]) selected.add(i);
                    if (selected.isEmpty()) { refreshDocChips(); return; }
                    askMandatoryForEach(names, selected, 0);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void askMandatoryForEach(String[] names, List<Integer> selected, int idx) {
        if (idx >= selected.size()) { refreshDocChips(); return; }
        String docName = names[selected.get(idx)];
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(docName)
                .setMessage("Is this document mandatory or optional?")
                .setPositiveButton("Mandatory", (d, w) -> {
                    selectedRequiredDocs.add(new RequiredDoc(docName, true));
                    askMandatoryForEach(names, selected, idx + 1);
                })
                .setNegativeButton("Optional", (d, w) -> {
                    selectedRequiredDocs.add(new RequiredDoc(docName, false));
                    askMandatoryForEach(names, selected, idx + 1);
                })
                .setCancelable(false)
                .show();
    }

    private void refreshDocChips() {
        binding.chipGroupDocs.removeAllViews();
        for (RequiredDoc rd : selectedRequiredDocs) {
            com.google.android.material.chip.Chip chip =
                    new com.google.android.material.chip.Chip(this);
            chip.setText(rd.getDocTypeName() + (rd.isMandatory() ? " *" : " (opt)"));
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                selectedRequiredDocs.remove(rd);
                refreshDocChips();
            });
            binding.chipGroupDocs.addView(chip);
        }
    }

    private void loadTeacherClasses() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;
        Constants.db().collection("teachers").whereEqualTo("userId", uid).limit(1).get()
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) return;
                    teacherClasses.clear();
                    teacherClasses.addAll(ClassAssignment.fromDocSnapshot(q.getDocuments().get(0)));
                    List<String> labels = new ArrayList<>();
                    for (ClassAssignment ca : teacherClasses) labels.add(ca.label());
                    runOnUiThread(() -> {
                        binding.spinnerClass.setAdapter(new ArrayAdapter<>(this,
                                android.R.layout.simple_dropdown_item_1line, labels));
                        binding.spinnerClass.setOnClickListener(v -> binding.spinnerClass.showDropDown());
                        if (!labels.isEmpty()) binding.spinnerClass.setText(labels.get(0), false);
                    });
                });
    }

    private void postNotice() {
        String title   = binding.etNoticeTitle.getText().toString().trim();
        String content = binding.etNoticeContent.getText().toString().trim();
        if (title.isEmpty())   { binding.etNoticeTitle.setError("Required"); return; }
        if (content.isEmpty()) { binding.etNoticeContent.setError("Required"); return; }

        setLoading(true);
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setPostedByUserId(session.getUserUid());
        notice.setPostedByName(session.getUserName());
        notice.setPostedByRole(session.getUserRole());

        if (isStateNotice) {
            notice.setNoticeType(Notice.TYPE_STATE);
            String selectedState = binding.spinnerState.getText().toString().trim();
            notice.setState("All India".equals(selectedState) ? "All" : selectedState);
        } else {
            notice.setNoticeType(Notice.TYPE_ORG);
            String schoolId = getIntent().hasExtra(EXTRA_SCHOOL_ID)
                    ? getIntent().getStringExtra(EXTRA_SCHOOL_ID) : session.getSchoolId();
            notice.setSchoolId(schoolId);

            // Audience (school admin only)
            if (User.ROLE_SCHOOL.equals(session.getUserRole())) {
                String aud = binding.spinnerAudience.getText().toString().trim();
                if ("Students Only".equals(aud)) notice.setAudience(Constants.AUDIENCE_STUDENTS);
                else if ("Faculty Only".equals(aud)) notice.setAudience(Constants.AUDIENCE_FACULTY);
                else notice.setAudience(Constants.AUDIENCE_ALL);
            }
        }

        // Tag class if teacher is posting
        if (User.ROLE_TEACHER.equals(session.getUserRole())) {
            String selectedLabel = binding.spinnerClass.getText().toString().trim();
            for (ClassAssignment ca : teacherClasses) {
                if (ca.label().equals(selectedLabel)) {
                    String tc = ca.getAssignedClass();
                    if (ca.getDivision() != null && !ca.getDivision().isEmpty())
                        tc += "-" + ca.getDivision();
                    notice.setTargetClass(tc);
                    break;
                }
            }
        }

        new NoticeRepository().postNotice(notice, new NoticeRepository.NoticeCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(PostNoticeActivity.this, "Notice posted!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(PostNoticeActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void postScholarship() {
        String title    = binding.etNoticeTitle.getText().toString().trim();
        String desc     = binding.etScholarshipDesc.getText().toString().trim();
        String amount   = binding.etAmount.getText().toString().trim();
        String deadline = binding.etDeadline.getText().toString().trim();
        String marksStr = binding.etMinMarks.getText().toString().trim();

        if (title.isEmpty())    { binding.etNoticeTitle.setError("Required"); return; }
        if (deadline.isEmpty()) { binding.etDeadline.setError("Enter deadline (DD/MM/YYYY)"); return; }

        int minMarks = 0;
        if (!marksStr.isEmpty()) {
            try { minMarks = Integer.parseInt(marksStr); }
            catch (NumberFormatException e) { binding.etMinMarks.setError("Enter a number"); return; }
        }

        setLoading(true);

        Scholarship s = new Scholarship();
        s.setTitle(title);
        s.setDescription(desc);
        s.setAmount(amount.isEmpty() ? "TBD" : amount);
        s.setDeadline(deadline);
        s.setMinMarksPercent(minMarks);
        s.setEligibleCategory(binding.spinnerCategory.getText().toString().trim());
        s.setEligibleClass(binding.spinnerEligibleClass.getText().toString().trim());
        s.setRequiredDocs(new ArrayList<>(selectedRequiredDocs));
        s.setPostedByUserId(session.getUserUid());
        s.setPostedByRole(session.getUserRole());

        if (User.ROLE_GOVERNMENT.equals(session.getUserRole())) {
            s.setIssuedBy("Government of Odisha");
            s.setSchoolId(null); // visible to all schools
        } else {
            s.setIssuedBy(session.getUserName());
            s.setSchoolId(session.getSchoolId());
        }

        new ScholarshipRepository().postScholarship(s, new ScholarshipRepository.ScholarshipCallback() {
            @Override
            public void onSuccess(String id) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(PostNoticeActivity.this, "Scholarship posted!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(PostNoticeActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void updateNotice() {
        String title   = binding.etNoticeTitle.getText().toString().trim();
        String content = binding.etNoticeContent.getText().toString().trim();
        if (title.isEmpty())   { binding.etNoticeTitle.setError("Required"); return; }
        if (content.isEmpty()) { binding.etNoticeContent.setError("Required"); return; }

        setLoading(true);
        Notice notice = new Notice();
        notice.setNoticeId(editingNoticeId);
        notice.setTitle(title);
        notice.setContent(content);
        if (isStateNotice) {
            String selectedState = binding.spinnerState.getText().toString().trim();
            notice.setState("All India".equals(selectedState) ? "All" : selectedState);
        }

        new NoticeRepository().updateNotice(notice, new NoticeRepository.NoticeCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(PostNoticeActivity.this, "Notice updated!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(PostNoticeActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.btnPost.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
