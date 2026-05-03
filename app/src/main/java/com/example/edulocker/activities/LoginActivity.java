package com.example.edulocker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edulocker.activities.govt.GovtDashboardActivity;
import com.example.edulocker.activities.parent.ParentDashboardActivity;
import com.example.edulocker.activities.school.SchoolDashboardActivity;
import com.example.edulocker.activities.teacher.TeacherDashboardActivity;
import com.example.edulocker.databinding.ActivityLoginBinding;
import com.example.edulocker.models.User;
import com.example.edulocker.repositories.AuthRepository;
import com.example.edulocker.repositories.StudentRepository;
import com.example.edulocker.utils.AccountManager;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private static final String GOVT_EMAIL    = "gov@edulocker.in";
    private static final String GOVT_PASSWORD = "gov123";

    private ActivityLoginBinding binding;
    private AuthRepository authRepo;
    private SessionManager session;
    private String lastLoginEmail;
    private String lastLoginPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        authRepo = new AuthRepository();
        session  = new SessionManager(this);

        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        binding.tvLoginViaAadhar.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlaceholderSettingsActivity.class);
            intent.putExtra(PlaceholderSettingsActivity.EXTRA_TITLE, "Login via Aadhaar");
            intent.putExtra(PlaceholderSettingsActivity.EXTRA_BODY,
                    "Aadhaar-based authentication requires official UIDAI infrastructure (AUA/KUA registration) and is not implemented in the EduLocker prototype.\n\nPlease use your Email and Password to login.");
            startActivity(intent);
        });

        binding.ivLogo.setOnLongClickListener(v -> {
            startActivity(new Intent(this, SeedDataActivity.class));
            return true;
        });
    }

    private void attemptLogin() {
        String email    = binding.etUniqueId.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter your ID and password", Toast.LENGTH_SHORT).show();
            return;
        }

        String loginEmail = email.contains("@") ? email : email.toLowerCase() + "@edulocker.in";
        lastLoginEmail    = loginEmail;
        lastLoginPassword = password;

        setLoading(true);
        authRepo.login(loginEmail, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                authRepo.getUserRole(user.getUid(), role -> runOnUiThread(() -> {
                    if (role == null && GOVT_EMAIL.equalsIgnoreCase(loginEmail)) {
                        // Auth account exists but Firestore doc is missing — auto-provision it
                        provisionGovtDoc(user.getUid());
                    } else {
                        setLoading(false);
                        fetchNameAndRoute(user.getUid(), role);
                    }
                }));
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    // If the government account doesn't exist yet, auto-create it
                    if (GOVT_EMAIL.equalsIgnoreCase(loginEmail)
                            && GOVT_PASSWORD.equals(password)
                            && isUserNotFoundError(error)) {
                        autoCreateGovtAccount();
                    } else {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this,
                                "Login failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    // Called when login succeeds but Firestore role doc is missing
    private void provisionGovtDoc(String uid) {
        com.google.firebase.auth.FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        String email = current != null && current.getEmail() != null ? current.getEmail() : GOVT_EMAIL;
        User user = new User(uid, "Government of Odisha", email, "", User.ROLE_GOVERNMENT);
        Constants.db()
                .collection(Constants.COL_USERS).document(uid).set(user)
                .addOnSuccessListener(v -> runOnUiThread(() -> {
                    setLoading(false);
                    fetchNameAndRoute(uid, User.ROLE_GOVERNMENT);
                }))
                .addOnFailureListener(e -> runOnUiThread(() -> {
                    setLoading(false);
                    fetchNameAndRoute(uid, User.ROLE_GOVERNMENT);
                }));
    }

    // Called when the govt Firebase Auth account itself doesn't exist yet
    private void autoCreateGovtAccount() {
        User govtUser = new User(null, "Government of Odisha", GOVT_EMAIL, "", User.ROLE_GOVERNMENT);
        authRepo.createAccount(GOVT_EMAIL, GOVT_PASSWORD, govtUser, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                // Sign out the secondary-auth instance, then sign in via primary auth
                FirebaseAuth.getInstance().signInWithEmailAndPassword(GOVT_EMAIL, GOVT_PASSWORD)
                        .addOnSuccessListener(result -> runOnUiThread(() -> {
                            setLoading(false);
                            fetchNameAndRoute(result.getUser().getUid(), User.ROLE_GOVERNMENT);
                        }))
                        .addOnFailureListener(e -> runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(LoginActivity.this,
                                    "Account created — please try logging in again.", Toast.LENGTH_LONG).show();
                        }));
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this,
                            "Setup failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void fetchNameAndRoute(String uid, String role) {
        if (role == null) {
            setLoading(false);
            String loginEmail = binding.etUniqueId.getText().toString().trim();
            boolean isGovt = GOVT_EMAIL.equalsIgnoreCase(loginEmail) || loginEmail.equalsIgnoreCase("gov");
            androidx.appcompat.app.AlertDialog.Builder builder =
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Account Not Configured")
                    .setMessage("This account exists in Firebase Auth but has no profile in the database.\n\n"
                            + "This usually means Firestore was not set up when the account was created.\n\n"
                            + "Tap 'Delete Account' to remove it so you can register again after setting up Firestore.")
                    .setNeutralButton("Cancel", null)
                    .setNegativeButton("Delete Account", (d, w) -> {
                        com.google.firebase.auth.FirebaseUser current =
                                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                        if (current != null) current.delete();
                        Toast.makeText(this, "Orphaned account deleted. You can now register again.", Toast.LENGTH_LONG).show();
                    });
            if (isGovt) {
                builder.setPositiveButton("Fix as Government", (d, w) -> {
                    setLoading(true);
                    provisionGovtDoc(uid);
                });
            }
            builder.show();
            return;
        }
        Constants.db().collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("name");
                    session.saveSession(uid, name != null ? name : "", role);
                    AccountManager.saveAccount(LoginActivity.this, lastLoginEmail,
                            lastLoginPassword, uid, name != null ? name : "", role);
                    fetchRoleSpecificIdAndRoute(uid, role);
                })
                .addOnFailureListener(e -> {
                    session.saveSession(uid, "", role);
                    AccountManager.saveAccount(LoginActivity.this, lastLoginEmail,
                            lastLoginPassword, uid, "", role);
                    fetchRoleSpecificIdAndRoute(uid, role);
                });
    }

    private void fetchRoleSpecificIdAndRoute(String uid, String role) {
        if (User.ROLE_SCHOOL.equals(role)) {
            Constants.db().collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        String schoolId = doc.getString("schoolId");
                        if (schoolId != null) session.saveSchoolId(schoolId);
                        route(role);
                    })
                    .addOnFailureListener(e -> route(role));
        } else if (User.ROLE_STUDENT.equals(role)) {
            new StudentRepository().getStudentByUserId(uid, new StudentRepository.StudentCallback() {
                @Override
                public void onSuccess(com.example.edulocker.models.Student student) {
                    session.savePassportId(student.getPassportId());
                    session.saveStudentSchoolId(student.getSchoolId());
                    runOnUiThread(() -> route(role));
                }
                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> route(role));
                }
            });
        } else if (User.ROLE_TEACHER.equals(role)) {
            Constants.db().collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        String schoolId = doc.getString("schoolId");
                        if (schoolId != null) session.saveSchoolId(schoolId);
                        route(role);
                    })
                    .addOnFailureListener(e -> route(role));
        } else {
            route(role);
        }
    }

    private void route(String role) {
        Intent intent;
        if (User.ROLE_GOVERNMENT.equals(role))   intent = new Intent(this, GovtDashboardActivity.class);
        else if (User.ROLE_SCHOOL.equals(role))  intent = new Intent(this, SchoolDashboardActivity.class);
        else if (User.ROLE_TEACHER.equals(role)) intent = new Intent(this, TeacherDashboardActivity.class);
        else if (User.ROLE_PARENT.equals(role))  intent = new Intent(this, ParentDashboardActivity.class);
        else                                     intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    private boolean isUserNotFoundError(String error) {
        if (error == null) return false;
        String lower = error.toLowerCase();
        return lower.contains("no user record")
                || lower.contains("there is no user")
                || lower.contains("user-not-found")
                || lower.contains("user not found");
    }

    private void showError(String message) {
        setLoading(false);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Login Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void setLoading(boolean loading) {
        binding.btnLogin.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
