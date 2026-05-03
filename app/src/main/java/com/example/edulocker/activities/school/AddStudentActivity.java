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

import com.example.edulocker.databinding.ActivityAddStudentBinding;
import com.example.edulocker.models.ClassAssignment;
import com.example.edulocker.models.School;
import com.example.edulocker.models.Student;
import com.example.edulocker.models.User;
import com.example.edulocker.repositories.AuthRepository;
import com.example.edulocker.repositories.SchoolRepository;
import com.example.edulocker.repositories.StudentRepository;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.Msg91Helper;
import com.example.edulocker.utils.PassportIdGenerator;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

public class AddStudentActivity extends AppCompatActivity {

    private ActivityAddStudentBinding binding;
    private School currentSchool;
    private SessionManager session;
    private List<String> teacherClassLabels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddStudentBinding.inflate(getLayoutInflater());
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

        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        binding.spinnerBloodGroup.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, bloodGroups));
        binding.spinnerBloodGroup.setOnClickListener(v -> binding.spinnerBloodGroup.showDropDown());

        String[] categories = {"General", "OBC", "SC", "ST"};
        binding.spinnerCategory.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories));
        binding.spinnerCategory.setOnClickListener(v -> binding.spinnerCategory.showDropDown());

        String[] classes = {
                "1", "2", "3", "4", "5", "6",
                "7", "8", "9", "10", "11", "12", "Custom"
        };
        binding.spinnerClass.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, classes));
        binding.spinnerClass.setOnClickListener(v -> binding.spinnerClass.showDropDown());
        binding.spinnerClass.setOnItemClickListener((parent, view, position, id) -> {
            boolean isCustom = "Custom".equals(classes[position]);
            binding.layoutCustomClass.setVisibility(isCustom ? View.VISIBLE : View.GONE);
            if (!isCustom) binding.etCustomClass.setText("");
        });

        loadSchool();
        if (User.ROLE_TEACHER.equals(session.getUserRole())) loadTeacherClasses();
        binding.btnAddStudent.setOnClickListener(v -> addStudent());
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

    private void loadTeacherClasses() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;

        Constants.db().collection(Constants.COL_TEACHERS)
                .whereEqualTo("userId", uid)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) return;
                    List<ClassAssignment> assignments =
                            ClassAssignment.fromDocSnapshot(snap.getDocuments().get(0));
                    if (assignments.isEmpty()) return;

                    teacherClassLabels.clear();
                    for (ClassAssignment ca : assignments) {
                        teacherClassLabels.add(ca.label());
                    }
                    runOnUiThread(() -> {
                        String[] options = teacherClassLabels.toArray(new String[0]);
                        binding.spinnerClass.setAdapter(new ArrayAdapter<>(
                                this, android.R.layout.simple_dropdown_item_1line, options));
                        // hide Custom class field — teacher can't use custom class
                        binding.layoutCustomClass.setVisibility(View.GONE);
                        binding.spinnerClass.setOnItemClickListener((parent, view, pos, id) -> {});
                    });
                });
    }

    private void addStudent() {
        String name          = binding.etStudentName.getText().toString().trim();
        String fatherName    = binding.etFatherName.getText().toString().trim();
        String motherName    = binding.etMotherName.getText().toString().trim();
        String dob           = binding.etDob.getText().toString().trim();
        String phone         = binding.etPhone.getText().toString().trim();
        String parentPhone   = binding.etParentPhone.getText().toString().trim();
        String address       = binding.etAddress.getText().toString().trim();
        String pinCode       = binding.etPinCode.getText().toString().trim();
        String section       = binding.etSection.getText().toString().trim();
        String bloodGroup       = binding.spinnerBloodGroup.getText().toString();
        String category         = binding.spinnerCategory.getText().toString();
        String selectedClass    = binding.spinnerClass.getText().toString();
        String studentClass;
        if ("Custom".equals(selectedClass)) {
            studentClass = binding.etCustomClass.getText().toString().trim();
            if (studentClass.isEmpty()) { binding.etCustomClass.setError("Enter the custom class"); return; }
        } else {
            studentClass = selectedClass;
        }
        String studentEmail  = binding.etStudentEmail.getText().toString().trim();
        String studentPass   = binding.etStudentPassword.getText().toString().trim();
        String parentEmail   = binding.etParentEmail.getText().toString().trim();
        String parentPass    = binding.etParentPassword.getText().toString().trim();

        if (name.isEmpty())                 { binding.etStudentName.setError("Required"); return; }
        if (fatherName.isEmpty())           { binding.etFatherName.setError("Required"); return; }
        if (dob.isEmpty())                  { binding.etDob.setError("DD/MM/YYYY required"); return; }
        if (parentPhone.length() != 10)     { binding.etParentPhone.setError("10-digit phone required"); return; }
        if (studentEmail.isEmpty() || !studentEmail.contains("@")) {
            binding.etStudentEmail.setError("Valid email required"); return;
        }
        if (studentPass.length() < 6)       { binding.etStudentPassword.setError("Min 6 characters"); return; }
        if (parentEmail.isEmpty() || !parentEmail.contains("@")) {
            binding.etParentEmail.setError("Valid email required"); return;
        }
        if (parentPass.length() < 6)        { binding.etParentPassword.setError("Min 6 characters"); return; }
        if (currentSchool == null) {
            Toast.makeText(this, "Organisation data not loaded, retry", Toast.LENGTH_SHORT).show(); return;
        }

        setLoading(true);

        DocumentReference schoolRef = Constants.db()
                .collection(Constants.COL_SCHOOLS)
                .document(currentSchool.getSchoolId());

        Constants.db().runTransaction(transaction -> {
            DocumentSnapshot snap = transaction.get(schoolRef);
            long current = snap.getLong("studentCount") != null ? snap.getLong("studentCount") : 0L;
            int nextSeq = (int)(current + 1);
            transaction.update(schoolRef, "studentCount", nextSeq);
            return nextSeq;
        }).addOnSuccessListener(seq -> {
            proceedWithCreation(seq, name, fatherName, motherName, dob,
                    phone, parentPhone, address, pinCode, section,
                    bloodGroup, category, studentClass,
                    studentEmail, studentPass, parentEmail, parentPass);
        }).addOnFailureListener(e -> runOnUiThread(() -> {
            setLoading(false);
            Toast.makeText(this, "Failed to reserve student slot: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }));
    }

    private void proceedWithCreation(int seq,
            String name, String fatherName, String motherName, String dob,
            String phone, String parentPhone, String address, String pinCode, String section,
            String bloodGroup, String category, String studentClass,
            String studentEmail, String studentPassword,
            String parentEmail, String parentPassword) {

        String passportId = PassportIdGenerator.generate(currentSchool, seq);

        Student student = new Student();
        student.setPassportId(passportId);
        student.setSchoolId(currentSchool.getSchoolId());
        student.setSchoolName(currentSchool.getName());
        student.setState(currentSchool.getState());
        student.setStateCode(currentSchool.getStateCode());
        student.setName(name);
        student.setFatherName(fatherName);
        student.setMotherName(motherName);
        student.setDob(dob);
        student.setContactNumber(phone.isEmpty() ? parentPhone : phone);
        student.setParentPhone(parentPhone);
        student.setAddress(address);
        student.setPinCode(pinCode);
        student.setStudentClass(studentClass);
        student.setSection(section);
        student.setBloodGroup(bloodGroup);
        student.setCategory(category);
        student.setKycStatus("Pending");
        student.setAttendancePercentage(100);
        student.setLoginEmail(studentEmail);
        student.setLoginPassword(studentPassword);
        student.setParentLoginEmail(parentEmail);
        student.setParentLoginPassword(parentPassword);
        student.setCreatedAt(Timestamp.now());

        // Step 1 — create parent Firebase Auth account
        User parentUser = new User(null, fatherName + " (Parent)", parentEmail, parentPhone, User.ROLE_PARENT);
        new AuthRepository().createAccount(parentEmail, parentPassword, parentUser,
                new AuthRepository.AuthCallback() {
                    @Override
                    public void onSuccess(com.google.firebase.auth.FirebaseUser parentFbUser) {
                        student.setParentUserId(parentFbUser.getUid());

                        // Step 2 — create student Firebase Auth account
                        User studentUser = new User(null, name, studentEmail, phone, User.ROLE_STUDENT);
                        new AuthRepository().createAccount(studentEmail, studentPassword, studentUser,
                                new AuthRepository.AuthCallback() {
                                    @Override
                                    public void onSuccess(com.google.firebase.auth.FirebaseUser studentFbUser) {
                                        student.setUserId(studentFbUser.getUid());

                                        // Step 3 — save student document
                                        new StudentRepository().saveStudent(student,
                                                new StudentRepository.StudentCallback() {
                                                    @Override
                                                    public void onSuccess(Student saved) {
                                                        Msg91Helper.sendCredentialsSms(
                                                                parentPhone,
                                                                fatherName + " (Parent)",
                                                                parentEmail, parentPassword,
                                                                new Msg91Helper.OtpCallback() {
                                                                    @Override public void onSuccess(String m) {}
                                                                    @Override public void onFailure(String e) {}
                                                                });
                                                        runOnUiThread(() -> {
                                                            setLoading(false);
                                                            showSuccessDialog(passportId,
                                                                    studentEmail, studentPassword,
                                                                    parentEmail, parentPassword);
                                                        });
                                                    }
                                                    @Override
                                                    public void onFailure(String error) {
                                                        runOnUiThread(() -> {
                                                            setLoading(false);
                                                            Toast.makeText(AddStudentActivity.this,
                                                                    "Error saving student: " + error,
                                                                    Toast.LENGTH_LONG).show();
                                                        });
                                                    }
                                                });
                                    }
                                    @Override
                                    public void onFailure(String error) {
                                        runOnUiThread(() -> {
                                            setLoading(false);
                                            Toast.makeText(AddStudentActivity.this,
                                                    "Error creating student account: " + error,
                                                    Toast.LENGTH_LONG).show();
                                        });
                                    }
                                });
                    }
                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(AddStudentActivity.this,
                                    "Error creating parent account: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private void showSuccessDialog(String passportId, String studentEmail, String studentPass,
                                   String parentEmail, String parentPass) {
        String msg = "Student added successfully!\n\n"
                + "Passport ID: " + passportId + "\n\n"
                + "Student Login:\n  Email: " + studentEmail + "\n  Pass: " + studentPass + "\n\n"
                + "Parent Login:\n  Email: " + parentEmail + "\n  Pass: " + parentPass + "\n\n"
                + "Parent credentials sent via SMS.";

        new AlertDialog.Builder(this)
                .setTitle("Student Registered")
                .setMessage(msg)
                .setPositiveButton("Copy & Close", (d, w) -> {
                    android.content.ClipboardManager clipboard =
                            (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("EduLocker", msg));
                    Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void setLoading(boolean loading) {
        binding.btnAddStudent.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
