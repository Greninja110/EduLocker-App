package com.example.edulocker.activities.school;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.edulocker.databinding.ActivityAddTeacherBinding;
import com.example.edulocker.models.School;
import com.example.edulocker.models.Teacher;
import com.example.edulocker.models.User;
import com.example.edulocker.repositories.AuthRepository;
import com.example.edulocker.repositories.SchoolRepository;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.Msg91Helper;
import com.example.edulocker.models.ClassAssignment;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddTeacherActivity extends AppCompatActivity {

    private ActivityAddTeacherBinding binding;
    private SessionManager session;
    private School currentSchool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTeacherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            int navBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            v.setPadding(0, statusBarHeight, 0, Math.max(imeBottom, navBottom));
            return insets;
        });
        binding.btnBack.setOnClickListener(v -> finish());
        session = new SessionManager(this);

        String[] classes = {
                "1", "2", "3", "4", "5", "6",
                "7", "8", "9", "10", "11", "12", "Custom"
        };
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, classes);
        binding.spinnerClass.setAdapter(classAdapter);
        binding.spinnerClass.setOnClickListener(v -> binding.spinnerClass.showDropDown());
        binding.spinnerClass.setOnItemClickListener((parent, view, position, id) -> {
            boolean isCustom = "Custom".equals(classes[position]);
            binding.layoutCustomClass.setVisibility(isCustom ? View.VISIBLE : View.GONE);
            if (!isCustom) binding.etCustomClass.setText("");
        });

        loadSchool();
        binding.btnAddTeacher.setOnClickListener(v -> addTeacher());
    }

    private void loadSchool() {
        new SchoolRepository().getSchool(session.getSchoolId(),
                new SchoolRepository.SchoolCallback() {
                    @Override
                    public void onSuccess(School school) { currentSchool = school; }
                    @Override
                    public void onFailure(String error) {}
                });
    }

    private void addTeacher() {
        String name          = binding.etTeacherName.getText().toString().trim();
        String subject       = binding.etSubject.getText().toString().trim();
        String phone         = binding.etPhone.getText().toString().trim();
        String selectedClass = binding.spinnerClass.getText().toString().trim();
        String division      = binding.etDivision.getText().toString().trim();
        String loginEmail    = binding.etLoginEmail.getText().toString().trim();
        String password      = binding.etLoginPassword.getText().toString().trim();

        String assignedClass;
        if ("Custom".equals(selectedClass)) {
            assignedClass = binding.etCustomClass.getText().toString().trim();
            if (assignedClass.isEmpty()) {
                binding.etCustomClass.setError("Enter the custom class");
                return;
            }
        } else {
            assignedClass = selectedClass;
        }

        if (name.isEmpty())                          { binding.etTeacherName.setError("Required"); return; }
        if (phone.length() != 10)                    { binding.etPhone.setError("10-digit phone required"); return; }
        if (assignedClass.isEmpty())                 { Toast.makeText(this, "Please select an assigned class", Toast.LENGTH_SHORT).show(); return; }
        if (loginEmail.isEmpty() || !loginEmail.contains("@")) { binding.etLoginEmail.setError("Valid email required"); return; }
        if (password.length() < 6)                   { binding.etLoginPassword.setError("Min 6 characters"); return; }
        if (currentSchool == null) {
            Toast.makeText(this, "Organisation data not loaded, retry", Toast.LENGTH_SHORT).show(); return;
        }

        setLoading(true);

        String schoolId  = currentSchool.getSchoolId();
        String teacherId = UUID.randomUUID().toString();

        final String finalClass    = assignedClass;
        final String finalDivision = division;

        User teacherUser = new User(null, name, loginEmail, phone, User.ROLE_TEACHER);
        teacherUser.setSchoolId(schoolId);
        new AuthRepository().createAccount(loginEmail, password, teacherUser,
                new AuthRepository.AuthCallback() {
                    @Override
                    public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                        Teacher teacher = new Teacher();
                        teacher.setTeacherId(teacherId);
                        teacher.setUserId(user.getUid());
                        teacher.setSchoolId(schoolId);
                        teacher.setName(name);
                        teacher.setSubject(subject);
                        teacher.setAssignedClass(finalClass);
                        teacher.setDivision(finalDivision);
                        List<ClassAssignment> assignments = new ArrayList<>();
                        assignments.add(new ClassAssignment(finalClass, finalDivision, subject));
                        teacher.setClassAssignments(assignments);
                        teacher.setPhone(phone);
                        teacher.setLoginEmail(loginEmail);
                        teacher.setLoginPassword(password);
                        teacher.setCreatedAt(Timestamp.now());

                        Constants.db().collection("teachers")
                                .document(teacherId).set(teacher)
                                .addOnSuccessListener(v -> {
                                    new SchoolRepository().incrementTeacherCount(schoolId);
                                    Msg91Helper.sendCredentialsSms(phone, name, loginEmail, password,
                                            new Msg91Helper.OtpCallback() {
                                                @Override public void onSuccess(String m) {}
                                                @Override public void onFailure(String e) {}
                                            });
                                    runOnUiThread(() -> {
                                        setLoading(false);
                                        String classDisplay = finalClass
                                                + (finalDivision.isEmpty() ? "" : " – " + finalDivision);
                                        String msg = "Faculty \"" + name + "\" added!\n"
                                                + "Class: " + classDisplay + "\n\n"
                                                + "Email: " + loginEmail + "\nPassword: " + password;
                                        new AlertDialog.Builder(AddTeacherActivity.this)
                                                .setTitle("Faculty Member Added")
                                                .setMessage(msg)
                                                .setPositiveButton("Copy & Close", (d, w) -> {
                                                    android.content.ClipboardManager cb =
                                                            (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                                    cb.setPrimaryClip(android.content.ClipData.newPlainText("Credentials", msg));
                                                    Toast.makeText(AddTeacherActivity.this, "Copied!", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                })
                                                .setCancelable(false)
                                                .show();
                                    });
                                })
                                .addOnFailureListener(e -> runOnUiThread(() -> {
                                    setLoading(false);
                                    Toast.makeText(AddTeacherActivity.this,
                                            "Failed to save teacher: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }));
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(AddTeacherActivity.this,
                                    "Failed to create account: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private void setLoading(boolean loading) {
        binding.btnAddTeacher.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
