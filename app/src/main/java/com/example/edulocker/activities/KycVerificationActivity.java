package com.example.edulocker.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.edulocker.databinding.ActivityKycVerificationBinding;
import com.example.edulocker.repositories.StudentRepository;
import com.example.edulocker.utils.Msg91Helper;
import com.example.edulocker.utils.SessionManager;

public class KycVerificationActivity extends AppCompatActivity {

    public static final String EXTRA_PASSPORT_ID = "kyc_passport_id";
    public static final String EXTRA_NAME        = "kyc_name";
    public static final String EXTRA_PHONE       = "kyc_phone";

    private ActivityKycVerificationBinding binding;
    private int currentStep = 0;
    private String passportId;
    private String phone;
    private boolean otpSent = false;
    private boolean videoRecorded = false;

    private final ActivityResultLauncher<Intent> videoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    (ActivityResult result) -> {
                        videoRecorded = true;
                        binding.tvVideoKycStatus.setText("Recording complete ✓");
                        binding.getRoot().postDelayed(() -> goToStep(3), 600);
                    });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) launchVideoIntent();
                        else Toast.makeText(this,
                                "Camera permission is required for Video KYC", Toast.LENGTH_LONG).show();
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityKycVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, top, 0, 0);
            return insets;
        });

        passportId = getIntent().getStringExtra(EXTRA_PASSPORT_ID);
        String name = getIntent().getStringExtra(EXTRA_NAME);
        phone = getIntent().getStringExtra(EXTRA_PHONE);

        binding.etStep1Name.setText(name != null ? name : "");
        binding.etStep1Uid.setText(passportId != null ? passportId : "");

        binding.btnBack.setOnClickListener(v -> {
            if (currentStep == 0) finish();
            else goToStep(currentStep - 1);
        });

        binding.tvSendOtp.setOnClickListener(v -> sendOtp());
        binding.btnAction.setOnClickListener(v -> handleAction());

        updateStepUi();
    }

    private void handleAction() {
        switch (currentStep) {
            case 0: validateStep1(); break;
            case 1: validateStep2(); break;
            case 2: startVideoRecording(); break;
            case 3: finalise(); break;
        }
    }

    // ── Step 1: confirm name ────────────────────────────────────────────────

    private void validateStep1() {
        String name = binding.etStep1Name.getText() != null
                ? binding.etStep1Name.getText().toString().trim() : "";
        if (name.isEmpty()) { binding.etStep1Name.setError("Please enter your name"); return; }
        goToStep(1);
    }

    // ── Step 2: Aadhaar entry + OTP ─────────────────────────────────────────

    private void validateStep2() {
        String aadhaar = binding.etStep2Aadhaar.getText() != null
                ? binding.etStep2Aadhaar.getText().toString().trim() : "";
        String otp = binding.etStep2Otp.getText() != null
                ? binding.etStep2Otp.getText().toString().trim() : "";

        if (aadhaar.length() != 12) {
            binding.etStep2Aadhaar.setError("Enter valid 12-digit Aadhaar number");
            return;
        }
        if (otp.isEmpty()) {
            Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!otpSent) {
            Toast.makeText(this, "Please send OTP first", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        Msg91Helper.verifyOtp(phone != null ? phone : "", otp, new Msg91Helper.OtpCallback() {
            @Override
            public void onSuccess(String message) {
                // OTP verified — save masked Aadhaar to Firestore, then advance
                String masked = maskAadhaar(aadhaar);
                if (passportId != null) {
                    new StudentRepository().saveAadhaarMasked(passportId, masked,
                            new StudentRepository.SimpleCallback() {
                                @Override public void onSuccess() {
                                    runOnUiThread(() -> { setLoading(false); goToStep(2); });
                                }
                                @Override public void onFailure(String error) {
                                    runOnUiThread(() -> { setLoading(false); goToStep(2); });
                                }
                            });
                } else {
                    runOnUiThread(() -> { setLoading(false); goToStep(2); });
                }
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(KycVerificationActivity.this,
                            "OTP verification failed: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // ── Step 3: real camera — 3-second video ────────────────────────────────

    private void startVideoRecording() {
        binding.tvVideoKycStatus.setText("Opening camera…");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            launchVideoIntent();
        }
    }

    private void launchVideoIntent() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 3);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);

        // Prefer front camera; fall back to rear if front is unavailable
        PackageManager pm = getPackageManager();
        boolean hasFront = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
        intent.putExtra("android.intent.extras.CAMERA_FACING",
                hasFront ? android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT
                         : android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
        // Secondary key used by some OEM camera apps
        intent.putExtra("android.intent.extras.LENS_FACING_FRONT", hasFront ? 1 : 0);
        intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", hasFront);

        if (intent.resolveActivity(pm) != null) {
            videoLauncher.launch(intent);
        } else {
            Toast.makeText(this, "No camera app found — skipping", Toast.LENGTH_SHORT).show();
            videoRecorded = true;
            goToStep(3);
        }
    }

    // ── Step 4: mark KYC Verified ───────────────────────────────────────────

    private void finalise() {
        if (passportId == null) { finish(); return; }
        setLoading(true);
        new StudentRepository().updateKycStatus(passportId, "Verified",
                new StudentRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(KycVerificationActivity.this,
                                    "ID verified successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(KycVerificationActivity.this,
                                    "Could not save status: " + error, Toast.LENGTH_LONG).show();
                            finish();
                        });
                    }
                });
    }

    // ── OTP ─────────────────────────────────────────────────────────────────

    private void sendOtp() {
        if (phone == null || phone.isEmpty()) {
            otpSent = true;
            Toast.makeText(this, "Demo mode — use OTP: 123456", Toast.LENGTH_LONG).show();
            return;
        }
        setLoading(true);
        Msg91Helper.sendOtp(phone, new Msg91Helper.OtpCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    setLoading(false);
                    otpSent = true;
                    Toast.makeText(KycVerificationActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(KycVerificationActivity.this,
                            "Failed to send OTP: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private String maskAadhaar(String raw) {
        if (raw.length() < 12) return raw;
        return raw.substring(0, 4) + " " + raw.substring(4, 6) + "** ****";
    }

    private void goToStep(int step) {
        currentStep = step;
        binding.viewFlipper.setDisplayedChild(step);
        updateStepUi();
        // Step 2 (UIDAI/OTP) — force focus + keyboard on the Aadhaar field
        // ViewFlipper doesn't transfer keyboard focus automatically on child switch
        if (step == 1) {
            binding.etStep2Aadhaar.post(() -> {
                binding.etStep2Aadhaar.requestFocus();
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.showSoftInput(binding.etStep2Aadhaar, InputMethodManager.SHOW_IMPLICIT);
            });
        }
    }

    private void updateStepUi() {
        int active   = getResources().getColor(com.example.edulocker.R.color.primary, null);
        int inactive = 0xFFE0E0E0;
        binding.stepBar1.setBackgroundColor(currentStep >= 0 ? active : inactive);
        binding.stepBar2.setBackgroundColor(currentStep >= 1 ? active : inactive);
        binding.stepBar3.setBackgroundColor(currentStep >= 2 ? active : inactive);
        binding.stepBar4.setBackgroundColor(currentStep >= 3 ? active : inactive);

        switch (currentStep) {
            case 0: binding.btnAction.setText("Continue");        break;
            case 1: binding.btnAction.setText("Verify OTP");      break;
            case 2: binding.btnAction.setText("Start Recording"); break;
            case 3: binding.btnAction.setText("Done");            break;
        }
        binding.btnAction.setEnabled(true);
    }

    private void setLoading(boolean loading) {
        binding.btnAction.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
