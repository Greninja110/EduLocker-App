package com.example.edulocker.activities.school;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.edulocker.R;
import com.example.edulocker.databinding.ActivityTeacherDetailBinding;
import com.example.edulocker.models.ClassAssignment;
import com.example.edulocker.utils.Constants;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherDetailActivity extends AppCompatActivity {

    public static final String EXTRA_NAME         = "t_name";
    public static final String EXTRA_SUBJECT      = "t_subject";
    public static final String EXTRA_CLASS        = "t_class";
    public static final String EXTRA_DIVISION     = "t_division";
    public static final String EXTRA_PHONE        = "t_phone";
    public static final String EXTRA_EMAIL        = "t_email";
    public static final String EXTRA_SCHOOL_ID    = "t_school_id";
    public static final String EXTRA_TEACHER_ID   = "t_teacher_id";

    private ActivityTeacherDetailBinding binding;
    private String teacherId;
    private final List<ClassAssignment> assignments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTeacherDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, top, 0, 0);
            return insets;
        });

        binding.btnBack.setOnClickListener(v -> finish());

        String name     = getIntent().getStringExtra(EXTRA_NAME);
        String phone    = getIntent().getStringExtra(EXTRA_PHONE);
        String email    = getIntent().getStringExtra(EXTRA_EMAIL);
        String schoolId = getIntent().getStringExtra(EXTRA_SCHOOL_ID);
        teacherId       = getIntent().getStringExtra(EXTRA_TEACHER_ID);

        if (name == null) name = "—";
        binding.tvToolbarTitle.setText(name);
        binding.tvName.setText(name);
        binding.tvAvatarInitials.setText(!name.isEmpty()
                ? String.valueOf(name.charAt(0)).toUpperCase() : "F");
        binding.tvPhone.setText(phone != null ? phone : "—");
        binding.tvEmail.setText(email != null ? email : "—");
        binding.tvSchoolId.setText(schoolId != null ? schoolId : "—");

        // Load classes from Firestore
        if (teacherId != null) loadClasses();

        binding.btnAddClass.setOnClickListener(v -> showAddClassDialog());
    }

    private void loadClasses() {
        Constants.db().collection("teachers").document(teacherId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    assignments.clear();
                    assignments.addAll(ClassAssignment.fromDocSnapshot(doc));
                    runOnUiThread(this::renderClasses);
                });
    }

    private void renderClasses() {
        binding.layoutClasses.removeAllViews();
        if (assignments.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("No classes assigned yet.");
            tv.setTextColor(getColor(R.color.text_secondary));
            tv.setTextSize(13);
            binding.layoutClasses.addView(tv);
            return;
        }
        for (ClassAssignment ca : assignments) {
            View row = LayoutInflater.from(this)
                    .inflate(android.R.layout.simple_list_item_1, binding.layoutClasses, false);
            TextView tv = row.findViewById(android.R.id.text1);
            tv.setText(ca.label());
            tv.setTextColor(getColor(R.color.text_primary));
            tv.setTextSize(14f);
            tv.setPadding(0, 8, 0, 8);
            binding.layoutClasses.addView(row);
        }
    }

    private void showAddClassDialog() {
        View dialogView = LayoutInflater.from(this).inflate(
                com.example.edulocker.R.layout.dialog_add_class, null);

        AutoCompleteTextView spinnerClass =
                dialogView.findViewById(com.example.edulocker.R.id.spinner_class);
        TextInputEditText etDivision =
                dialogView.findViewById(com.example.edulocker.R.id.et_division);
        TextInputEditText etSubject  =
                dialogView.findViewById(com.example.edulocker.R.id.et_subject);
        TextInputLayout layoutCustomClass =
                dialogView.findViewById(com.example.edulocker.R.id.layout_custom_class);
        TextInputEditText etCustomClass =
                dialogView.findViewById(com.example.edulocker.R.id.et_custom_class);

        String[] classes = {"1","2","3","4","5","6","7","8","9","10","11","12","Custom"};
        spinnerClass.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, classes));
        spinnerClass.setOnClickListener(v -> spinnerClass.showDropDown());
        spinnerClass.setOnItemClickListener((p, v, pos, id) -> {
            boolean custom = "Custom".equals(classes[pos]);
            layoutCustomClass.setVisibility(custom ? View.VISIBLE : View.GONE);
            if (!custom) etCustomClass.setText("");
        });

        new AlertDialog.Builder(this)
                .setTitle("Assign New Class")
                .setView(dialogView)
                .setPositiveButton("Assign", (d, w) -> {
                    String sel = spinnerClass.getText().toString().trim();
                    String cls = "Custom".equals(sel)
                            ? (etCustomClass.getText() != null
                                    ? etCustomClass.getText().toString().trim() : "")
                            : sel;
                    String div = etDivision.getText() != null
                            ? etDivision.getText().toString().trim() : "";
                    String sub = etSubject.getText() != null
                            ? etSubject.getText().toString().trim() : "";

                    if (cls.isEmpty()) {
                        Toast.makeText(this, "Select a class", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveClassToFirestore(cls, div, sub);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveClassToFirestore(String cls, String division, String subject) {
        if (teacherId == null) return;
        Map<String, Object> entry = new HashMap<>();
        entry.put("assignedClass", cls);
        entry.put("division", division);
        entry.put("subject", subject);

        Constants.db().collection("teachers").document(teacherId)
                .update("classAssignments", FieldValue.arrayUnion(entry))
                .addOnSuccessListener(v -> {
                    ClassAssignment ca = new ClassAssignment(cls, division, subject);
                    assignments.add(ca);
                    runOnUiThread(() -> {
                        renderClasses();
                        Toast.makeText(this, "Class assigned!", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
