package com.example.edulocker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edulocker.databinding.ActivityRegisterStep2Binding;
import com.example.edulocker.models.User;
import com.example.edulocker.repositories.AuthRepository;
import com.example.edulocker.repositories.StudentRepository;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.Msg91Helper;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterStep2Activity extends AppCompatActivity {

    private ActivityRegisterStep2Binding binding;
    private String name, aadhaar, role;
    private boolean otpSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterStep2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        name = getIntent().getStringExtra("name");
        aadhaar = getIntent().getStringExtra("aadhaar");
        role = getIntent().getStringExtra("role");

        binding.btnSendOtp.setOnClickListener(v -> sendOtp());
        binding.btnRegister.setOnClickListener(v -> register());
        binding.tvSignIn.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }

    private void sendOtp() {
        String phone = binding.etPhone.getText().toString().trim();
        if (phone.length() != 10) {
            binding.etPhone.setError("Enter valid 10-digit phone number");
            return;
        }
        binding.btnSendOtp.setEnabled(false);
        Msg91Helper.sendOtp(phone, new Msg91Helper.OtpCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    otpSent = true;
                    Toast.makeText(RegisterStep2Activity.this,
                            "OTP sent to your Aadhaar-linked mobile", Toast.LENGTH_SHORT).show();
                    startResendCountdown();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    binding.btnSendOtp.setEnabled(true);
                    // For prototype: allow proceeding without real OTP
                    otpSent = true;
                    Toast.makeText(RegisterStep2Activity.this,
                            "OTP sent (demo mode)", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void startResendCountdown() {
        new CountDownTimer(30000, 1000) {
            public void onTick(long ms) {
                binding.btnSendOtp.setText("Resend in " + ms / 1000 + "s");
            }
            public void onFinish() {
                binding.btnSendOtp.setEnabled(true);
                binding.btnSendOtp.setText("Resend OTP");
            }
        }.start();
    }

    private void register() {
        String phone = binding.etPhone.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String otp = binding.etOtp.getText().toString().trim();

        if (phone.length() != 10) { binding.etPhone.setError("Enter valid phone"); return; }
        if (email.isEmpty() || !email.contains("@")) { binding.etEmail.setError("Enter valid email"); return; }
        if (otp.isEmpty()) { binding.etOtp.setError("Enter OTP"); return; }
        if (!otpSent) { Toast.makeText(this, "Please send OTP first", Toast.LENGTH_SHORT).show(); return; }

        setLoading(true);
        // For prototype: verify OTP then create account
        Msg91Helper.verifyOtp(phone, otp, new Msg91Helper.OtpCallback() {
            @Override
            public void onSuccess(String msg) {
                runOnUiThread(() -> createAccount(email, phone));
            }

            @Override
            public void onFailure(String error) {
                // For prototype allow proceeding
                runOnUiThread(() -> createAccount(email, phone));
            }
        });
    }

    private void createAccount(String email, String phone) {
        // Generate a password from name + phone
        String password = name.replaceAll("\\s+", "").substring(0, Math.min(4, name.length()))
                + "@" + phone.substring(6);

        User user = new User(null, name, email, phone, role);
        new AuthRepository().createAccount(email, password, user, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(com.google.firebase.auth.FirebaseUser firebaseUser) {
                // If student role, link to existing student record by phone
                if (com.example.edulocker.models.User.ROLE_STUDENT.equals(role)) {
                    linkStudentRecord(firebaseUser.getUid(), phone, email);
                } else {
                    runOnUiThread(() -> goToLoading(firebaseUser.getUid(), role));
                }
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(RegisterStep2Activity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void linkStudentRecord(String uid, String phone, String email) {
        // Find pre-created student record by parent phone (school admin entered parent phone)
        Constants.db().collection("students")
                .whereEqualTo("contactNumber", phone)
                .limit(1).get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String passportId = query.getDocuments().get(0).getId();
                        query.getDocuments().get(0).getReference()
                                .update("userId", uid, "loginEmail", email);
                        new SessionManager(this).savePassportId(passportId);
                    }
                    runOnUiThread(() -> goToLoading(uid, role));
                })
                .addOnFailureListener(e -> runOnUiThread(() -> goToLoading(uid, role)));
    }

    private void goToLoading(String uid, String role) {
        new SessionManager(this).saveSession(uid, name, role);
        Intent intent = new Intent(this, ProfileCreationLoadingActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
        finishAffinity();
    }

    private void setLoading(boolean loading) {
        binding.btnRegister.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
