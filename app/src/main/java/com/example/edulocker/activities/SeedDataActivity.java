package com.example.edulocker.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edulocker.databinding.ActivitySeedDataBinding;
import com.example.edulocker.models.School;
import com.example.edulocker.models.Student;
import com.example.edulocker.models.Teacher;
import com.example.edulocker.models.User;
import com.example.edulocker.utils.Constants;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SeedDataActivity extends AppCompatActivity {

    private static final String SCHOOL_ID    = "GHSS-PKS";
    private static final String PASSPORT_ID  = "MH-PKS-001";

    private ActivitySeedDataBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private StringBuilder log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySeedDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        db   = Constants.db();
        log  = new StringBuilder();

        binding.btnSeed.setOnClickListener(v -> startSeeding());
        binding.tvBack.setOnClickListener(v -> finish());
    }

    private void startSeeding() {
        binding.btnSeed.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);
        log.setLength(0);
        appendLog("Starting demo user setup...\n");
        createGovt();
    }

    // ── 1. Government ─────────────────────────────────────────────────────────

    private void createGovt() {
        appendLog("Creating government user...");
        // 15-second timeout guard — fires if Firebase Auth never responds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (log.toString().contains("Creating government user...") &&
                    !log.toString().contains("Government user")) {
                appendLog("✗ Timeout — is Email/Password auth enabled in Firebase Console?");
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSeed.setEnabled(true);
            }
        }, 15000);
        auth.createUserWithEmailAndPassword("gov@edulocker.in", "gov123")
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    User user = new User(uid, "Government of Odisha", "gov@edulocker.in", "", User.ROLE_GOVERNMENT);
                    db.collection(Constants.COL_USERS).document(uid).set(user)
                            .addOnSuccessListener(v -> appendLog("  ✓ Firestore doc saved"))
                            .addOnFailureListener(e -> appendLog("  ⚠ Firestore: " + e.getMessage()));
                    appendLog("✓ Government user created");
                    createSchool();
                })
                .addOnFailureListener(e -> {
                    appendLog("✗ Govt Auth: " + e.getMessage());
                    createSchool();
                });
    }

    // ── 2. School (org) ───────────────────────────────────────────────────────

    private void createSchool() {
        appendLog("Creating school/org user...");
        auth.createUserWithEmailAndPassword("org@gmail.com", "org123")
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();

                    // User doc
                    Map<String, Object> userDoc = new HashMap<>();
                    userDoc.put("uid", uid);
                    userDoc.put("name", "Govt. Higher Secondary School");
                    userDoc.put("email", "org@gmail.com");
                    userDoc.put("phone", "");
                    userDoc.put("role", User.ROLE_SCHOOL);
                    userDoc.put("schoolId", SCHOOL_ID);
                    userDoc.put("createdAt", Timestamp.now());
                    db.collection(Constants.COL_USERS).document(uid).set(userDoc);

                    // School doc
                    School school = new School();
                    school.setSchoolId(SCHOOL_ID);
                    school.setName("Govt. Higher Secondary School");
                    school.setType("higher_secondary");
                    school.setDistrict("Kandhamal");
                    school.setDistrictCode("KMD");
                    school.setState("Odisha");
                    school.setSchoolCode("PKS");
                    school.setLoginEmail("org@gmail.com");
                    school.setLoginPassword("org123");
                    school.setPrincipalName("Subrat Ratan Behera");
                    school.setAddress("Phulbani, Kandhamal, Odisha");
                    school.setPhone("9876500000");
                    school.setStudentCount(1);
                    school.setTeacherCount(1);
                    school.setCreatedAt(Timestamp.now());
                    db.collection(Constants.COL_SCHOOLS).document(SCHOOL_ID).set(school)
                            .addOnSuccessListener(v -> appendLog("  ✓ School Firestore doc saved"))
                            .addOnFailureListener(e -> appendLog("  ⚠ School Firestore: " + e.getMessage()));
                    appendLog("✓ School user + school record created");
                    createParent();
                })
                .addOnFailureListener(e -> {
                    appendLog("✗ School Auth: " + e.getMessage());
                    createParent();
                });
    }

    // ── 3. Parent ─────────────────────────────────────────────────────────────

    private void createParent() {
        appendLog("Creating parent user...");
        auth.createUserWithEmailAndPassword("parent@gmail.com", "parent123")
                .addOnSuccessListener(result -> {
                    String parentUid = result.getUser().getUid();
                    User user = new User(parentUid, "Kumar Samantaray", "parent@gmail.com", "9876543210", User.ROLE_PARENT);
                    db.collection(Constants.COL_USERS).document(parentUid).set(user)
                            .addOnSuccessListener(v -> appendLog("  ✓ Parent Firestore doc saved"))
                            .addOnFailureListener(e -> appendLog("  ⚠ Parent Firestore: " + e.getMessage()));
                    appendLog("✓ Parent user created");
                    createStudent(parentUid);
                })
                .addOnFailureListener(e -> {
                    appendLog("✗ Parent Auth: " + e.getMessage());
                    createStudent(null);
                });
    }

    // ── 4. Student ────────────────────────────────────────────────────────────

    private void createStudent(String parentUid) {
        appendLog("Creating student user...");
        auth.createUserWithEmailAndPassword("student@gmail.com", "student123")
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();

                    User user = new User(uid, "Priyanshu Kumar Samantaray", "student@gmail.com", "9876543210", User.ROLE_STUDENT);
                    db.collection(Constants.COL_USERS).document(uid).set(user);

                    Student student = new Student();
                    student.setPassportId(PASSPORT_ID);
                    student.setUserId(uid);
                    student.setSchoolId(SCHOOL_ID);
                    student.setSchoolName("Govt. Higher Secondary School");
                    student.setName("Priyanshu Kumar Samantaray");
                    student.setDob("01/01/2008");
                    student.setBloodGroup("B +ve");
                    student.setFatherName("Kumar Samantaray");
                    student.setMotherName("Sushmita Samantaray");
                    student.setAddress("Masterpada, Phulbani, Kandhamal");
                    student.setPinCode("762001");
                    student.setStudentClass("12 SCIENCE");
                    student.setSection("A");
                    student.setContactNumber("9876543210");
                    student.setAadhaarMasked("3431 56** ****");
                    student.setKycStatus("Verified");
                    student.setCategory("General");
                    student.setParentUserId(parentUid != null ? parentUid : "");
                    student.setParentPhone("9876543210");
                    student.setLoginEmail("student@gmail.com");
                    student.setLoginPassword("student123");
                    student.setAttendancePercentage(76);
                    student.setCreatedAt(Timestamp.now());

                    db.collection(Constants.COL_STUDENTS).document(PASSPORT_ID).set(student)
                            .addOnSuccessListener(v -> appendLog("  ✓ Student Firestore doc saved"))
                            .addOnFailureListener(e -> appendLog("  ⚠ Student Firestore: " + e.getMessage()));
                    appendLog("✓ Student user created (Passport: " + PASSPORT_ID + ")");
                    createTeacher();
                })
                .addOnFailureListener(e -> {
                    appendLog("✗ Student Auth: " + e.getMessage());
                    createTeacher();
                });
    }

    // ── 5. Teacher ────────────────────────────────────────────────────────────

    private void createTeacher() {
        appendLog("Creating teacher user...");
        auth.createUserWithEmailAndPassword("teacher@gmail.com", "teacher123")
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    String teacherId = UUID.randomUUID().toString();

                    Map<String, Object> userDoc = new HashMap<>();
                    userDoc.put("uid", uid);
                    userDoc.put("name", "Demo Teacher");
                    userDoc.put("email", "teacher@gmail.com");
                    userDoc.put("phone", "9876500001");
                    userDoc.put("role", User.ROLE_TEACHER);
                    userDoc.put("schoolId", SCHOOL_ID);
                    userDoc.put("createdAt", Timestamp.now());
                    db.collection(Constants.COL_USERS).document(uid).set(userDoc);

                    Teacher teacher = new Teacher();
                    teacher.setTeacherId(teacherId);
                    teacher.setUserId(uid);
                    teacher.setSchoolId(SCHOOL_ID);
                    teacher.setSchoolName("Govt. Higher Secondary School");
                    teacher.setName("Demo Teacher");
                    teacher.setSubject("Science");
                    teacher.setAssignedClass("12");
                    teacher.setPhone("9876500001");
                    teacher.setLoginEmail("teacher@gmail.com");
                    teacher.setLoginPassword("teacher123");
                    teacher.setCreatedAt(Timestamp.now());

                    db.collection(Constants.COL_TEACHERS).document(teacherId).set(teacher)
                            .addOnSuccessListener(v -> appendLog("  ✓ Teacher Firestore doc saved"))
                            .addOnFailureListener(e -> appendLog("  ⚠ Teacher Firestore: " + e.getMessage()));
                    appendLog("✓ Teacher user created");
                    onSeedComplete();
                })
                .addOnFailureListener(e -> {
                    appendLog("✗ Teacher Auth: " + e.getMessage());
                    onSeedComplete();
                });
    }

    // ── Done ──────────────────────────────────────────────────────────────────

    private void onSeedComplete() {
        auth.signOut();
        appendLog("\n✅ Setup complete! Sign out done.");
        appendLog("You can now log in with any of the 5 demo accounts.");
        runOnUiThread(() -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSeed.setEnabled(true);
            binding.btnSeed.setText("Done — Go Back");
            binding.btnSeed.setOnClickListener(v -> finish());
        });
    }

    private void appendLog(String message) {
        log.append(message).append("\n");
        runOnUiThread(() -> binding.tvLog.setText(log.toString()));
    }
}
