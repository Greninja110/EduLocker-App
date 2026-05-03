package com.example.edulocker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edulocker.databinding.ActivityRegisterStep1Binding;
import com.example.edulocker.models.User;

public class RegisterStep1Activity extends AppCompatActivity {

    private ActivityRegisterStep1Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterStep1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String[] roles = {"Student", "Parent", "Teacher"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, roles);
        binding.spinnerRole.setAdapter(adapter);

        binding.btnContinue.setOnClickListener(v -> proceed());
        binding.tvSignIn.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void proceed() {
        String name = binding.etName.getText().toString().trim();
        String aadhaar = binding.etAadhaar.getText().toString().trim();
        String role = binding.spinnerRole.getText().toString().trim();

        if (name.isEmpty()) {
            binding.etName.setError("Enter your name");
            return;
        }
        if (aadhaar.length() < 12) {
            binding.etAadhaar.setError("Enter valid 12-digit Aadhaar number");
            return;
        }
        if (role.isEmpty()) {
            Toast.makeText(this, "Please select your role", Toast.LENGTH_SHORT).show();
            return;
        }

        String mappedRole = mapRole(role);
        Intent intent = new Intent(this, RegisterStep2Activity.class);
        intent.putExtra("name", name);
        intent.putExtra("aadhaar", aadhaar);
        intent.putExtra("role", mappedRole);
        startActivity(intent);
    }

    private String mapRole(String selected) {
        switch (selected.toLowerCase()) {
            case "parent": return User.ROLE_PARENT;
            case "teacher": return User.ROLE_TEACHER;
            default: return User.ROLE_STUDENT;
        }
    }
}
