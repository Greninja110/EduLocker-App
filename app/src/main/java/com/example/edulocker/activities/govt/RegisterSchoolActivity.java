package com.example.edulocker.activities.govt;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.edulocker.databinding.ActivityRegisterSchoolBinding;
import com.example.edulocker.models.School;
import com.example.edulocker.models.User;
import com.example.edulocker.repositories.AuthRepository;
import com.example.edulocker.repositories.SchoolRepository;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.PassportIdGenerator;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.Timestamp;

import java.util.UUID;

public class RegisterSchoolActivity extends AppCompatActivity {

    private ActivityRegisterSchoolBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterSchoolBinding.inflate(getLayoutInflater());
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

        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, Constants.STATE_NAMES);
        binding.spinnerState.setAdapter(stateAdapter);
        binding.spinnerState.setOnClickListener(v -> binding.spinnerState.showDropDown());

        String[] types = {
                "Higher Secondary", "High School", "Primary School",
                "College", "University", "Institute", "Vocational"
        };
        binding.spinnerType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, types));
        binding.spinnerType.setOnClickListener(v -> binding.spinnerType.showDropDown());

        binding.btnRegisterSchool.setOnClickListener(v -> registerOrganisation());
    }

    private void registerOrganisation() {
        String name      = binding.etSchoolName.getText().toString().trim();
        String state     = binding.spinnerState.getText().toString().trim();
        String district  = binding.etDistrict.getText() != null
                ? binding.etDistrict.getText().toString().trim() : "";
        String type      = binding.spinnerType.getText().toString().trim();
        String adminName = binding.etPrincipalName.getText().toString().trim();
        String address   = binding.etAddress.getText().toString().trim();
        String phone     = binding.etPhone.getText().toString().trim();

        String loginEmail = binding.etLoginEmail.getText().toString().trim();
        String password   = binding.etLoginPassword.getText().toString().trim();

        if (name.isEmpty())         { binding.etSchoolName.setError("Required"); return; }
        if (state.isEmpty())        { Toast.makeText(this, "Please select a state", Toast.LENGTH_SHORT).show(); return; }
        if (type.isEmpty())         { Toast.makeText(this, "Please select organisation type", Toast.LENGTH_SHORT).show(); return; }
        if (loginEmail.isEmpty())   { binding.etLoginEmail.setError("Required"); return; }
        if (!loginEmail.contains("@")) { binding.etLoginEmail.setError("Enter a valid email"); return; }
        if (password.length() < 6)  { binding.etLoginPassword.setError("Min 6 characters"); return; }

        setLoading(true);

        String stateCode    = Constants.getStateCode(state);
        String districtCode = PassportIdGenerator.generateDistrictCode(district);
        String schoolCode   = PassportIdGenerator.generateSchoolCode(name);
        String schoolId     = UUID.randomUUID().toString();

        School school = new School();
        school.setSchoolId(schoolId);
        school.setName(name);
        school.setType(type);
        school.setState(state);
        school.setStateCode(stateCode);
        school.setDistrict(district);
        school.setDistrictCode(districtCode);
        school.setSchoolCode(schoolCode);
        school.setLoginEmail(loginEmail);
        school.setLoginPassword(password);  // @Exclude — not written to Firestore
        school.setRegisteredByGovtId(new SessionManager(this).getUserUid());
        school.setPrincipalName(adminName);
        school.setAddress(address);
        school.setPhone(phone);
        school.setStudentCount(0);
        school.setTeacherCount(0);
        school.setCreatedAt(Timestamp.now());

        // schoolId is embedded in User doc from the start — no separate update() needed
        User orgUser = new User(null, name, loginEmail, phone, User.ROLE_SCHOOL);
        orgUser.setSchoolId(schoolId);

        new AuthRepository().createAccount(loginEmail, password, orgUser,
                new AuthRepository.AuthCallback() {
                    @Override
                    public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                        new SchoolRepository().saveSchool(school,
                                new SchoolRepository.SchoolCallback() {
                                    @Override
                                    public void onSuccess(School saved) {
                                        runOnUiThread(() -> {
                                            setLoading(false);
                                            showCredentialsDialog(name, loginEmail, password,
                                                    schoolCode, state, district);
                                        });
                                    }
                                    @Override
                                    public void onFailure(String error) {
                                        runOnUiThread(() -> {
                                            setLoading(false);
                                            showError("Database Save Failed",
                                                    "Login account was created but the organisation record failed to save.\n\n"
                                                    + "Error: " + error + "\n\n"
                                                    + "Most likely cause: Firestore security rules have expired (test mode rules last 30 days).\n"
                                                    + "Go to Firebase Console → Firestore → Rules and extend the expiry date.");
                                        });
                                    }
                                });
                    }
                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            showError("Registration Failed",
                                    "Could not create login account for \"" + name + "\".\n\n"
                                    + "Error: " + error + "\n\n"
                                    + "Common causes:\n"
                                    + "• This email is already registered\n"
                                    + "• No internet connection\n"
                                    + "• Firebase Auth not enabled (enable Email/Password in Firebase Console)");
                        });
                    }
                });
    }

    private void showCredentialsDialog(String orgName, String email, String password,
                                       String orgCode, String state, String district) {
        String message = "Organisation \"" + orgName + "\" registered successfully!\n\n"
                + "State: " + state + "\n"
                + (district.isEmpty() ? "" : "District: " + district + "\n")
                + "Org Code: " + orgCode + "\n\n"
                + "── Admin Login Credentials ──\n"
                + "Email:    " + email + "\n"
                + "Password: " + password + "\n\n"
                + "The admin can log in and register faculty members.";

        new AlertDialog.Builder(this)
                .setTitle("Organisation Registered")
                .setMessage(message)
                .setPositiveButton("Copy & Close", (d, w) -> {
                    android.content.ClipboardManager clipboard =
                            (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(
                            android.content.ClipData.newPlainText("EduLocker Credentials", message));
                    Toast.makeText(this, "Credentials copied!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showError(String title, String message) {
        if (isFinishing()) return;
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void setLoading(boolean loading) {
        binding.btnRegisterSchool.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
