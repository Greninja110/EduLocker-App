package com.example.edulocker.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.edulocker.databinding.ActivityAccountDetailsBinding;
import com.example.edulocker.databinding.ItemReadonlyFieldBinding;
import com.example.edulocker.models.Student;
import com.example.edulocker.repositories.StudentRepository;
import com.example.edulocker.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class AccountDetailsActivity extends AppCompatActivity {

    private ActivityAccountDetailsBinding binding;
    private StudentRepository repo;
    private String passportId;
    private Student currentStudent;

    private static final String[] BLOOD_GROUPS =
            {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Light status bar icons won't work on dark header — keep them white (default)
        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);

        // Push header below status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, statusBarHeight, 0, 0);
            return insets;
        });

        repo = new StudentRepository();
        SessionManager session = new SessionManager(this);
        passportId = session.getPassportId();
        String role = session.getUserRole();

        setupBloodGroupDropdown();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveChanges());

        if (!"student".equals(role) || passportId == null) {
            showEmptyState();
        } else {
            loadStudentData();
        }
    }

    private void setupBloodGroupDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, BLOOD_GROUPS);
        binding.etBloodGroup.setAdapter(adapter);
        binding.etBloodGroup.setOnClickListener(v -> binding.etBloodGroup.showDropDown());
    }

    private void showEmptyState() {
        binding.tvHeaderPassport.setText("—");
        setReadonlyRow(binding.rowPassportId, "Passport ID", "—");
        setReadonlyRow(binding.rowSchool,     "Organisation", "—");
        setReadonlyRow(binding.rowClass,      "Class", "—");
        setReadonlyRow(binding.rowCategory,   "Category", "—");
        setReadonlyRow(binding.rowKyc,        "KYC Status", "—");
        setReadonlyRow(binding.rowEmail,      "Login Email", "—");
        binding.btnSave.setEnabled(false);
    }

    private void loadStudentData() {
        if (passportId == null) return;
        binding.btnSave.setEnabled(false);

        repo.getStudent(passportId, new StudentRepository.StudentCallback() {
            @Override
            public void onSuccess(Student student) {
                currentStudent = student;
                runOnUiThread(() -> populateFields(student));
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(AccountDetailsActivity.this,
                                "Failed to load: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void populateFields(Student student) {
        // Header
        binding.tvHeaderPassport.setText(student.getPassportId());

        // Read-only rows (binding.rowXxx is ItemReadonlyFieldBinding due to <include> with id)
        setReadonlyRow(binding.rowPassportId, "Passport ID", student.getPassportId());
        setReadonlyRow(binding.rowSchool, "School", student.getSchoolName());
        setReadonlyRow(binding.rowClass, "Class", student.getStudentClass());
        setReadonlyRow(binding.rowCategory, "Category", student.getCategory());
        setReadonlyRow(binding.rowKyc, "KYC Status", student.getKycStatus());
        setReadonlyRow(binding.rowEmail, "Login Email", student.getLoginEmail());

        // Editable fields
        if (student.getBloodGroup() != null)
            binding.etBloodGroup.setText(student.getBloodGroup(), false);
        if (student.getContactNumber() != null)
            binding.etContact.setText(student.getContactNumber());
        if (student.getFatherName() != null)
            binding.etFatherName.setText(student.getFatherName());
        if (student.getMotherName() != null)
            binding.etMotherName.setText(student.getMotherName());
        if (student.getAddress() != null)
            binding.etAddress.setText(student.getAddress());
        if (student.getPinCode() != null)
            binding.etPinCode.setText(student.getPinCode());

        binding.btnSave.setEnabled(true);
    }

    private void setReadonlyRow(ItemReadonlyFieldBinding row, String label, String value) {
        row.tvLabel.setText(label);
        row.tvValue.setText(value != null && !value.isEmpty() ? value : "—");
    }

    private void saveChanges() {
        if (currentStudent == null || passportId == null) return;

        String bloodGroup = binding.etBloodGroup.getText().toString().trim();
        String contact = binding.etContact.getText() != null
                ? binding.etContact.getText().toString().trim() : "";
        String fatherName = binding.etFatherName.getText() != null
                ? binding.etFatherName.getText().toString().trim() : "";
        String motherName = binding.etMotherName.getText() != null
                ? binding.etMotherName.getText().toString().trim() : "";
        String address = binding.etAddress.getText() != null
                ? binding.etAddress.getText().toString().trim() : "";
        String pinCode = binding.etPinCode.getText() != null
                ? binding.etPinCode.getText().toString().trim() : "";

        if (!contact.isEmpty() && contact.length() != 10) {
            Toast.makeText(this, "Contact number must be 10 digits", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pinCode.isEmpty() && pinCode.length() != 6) {
            Toast.makeText(this, "Pin code must be 6 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("bloodGroup", bloodGroup);
        updates.put("contactNumber", contact);
        updates.put("fatherName", fatherName);
        updates.put("motherName", motherName);
        updates.put("address", address);
        updates.put("pinCode", pinCode);

        binding.btnSave.setEnabled(false);
        binding.btnSave.setText("Saving…");

        repo.updateStudentFields(passportId, updates, new StudentRepository.StudentCallback() {
            @Override
            public void onSuccess(Student student) {
                currentStudent = student;
                runOnUiThread(() -> {
                    binding.btnSave.setEnabled(true);
                    binding.btnSave.setText("Save Changes");
                    Toast.makeText(AccountDetailsActivity.this,
                            "Details updated successfully", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    binding.btnSave.setEnabled(true);
                    binding.btnSave.setText("Save Changes");
                    Toast.makeText(AccountDetailsActivity.this,
                            "Save failed: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
