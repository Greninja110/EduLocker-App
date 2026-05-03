package com.example.edulocker.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edulocker.activities.parent.ParentDashboardActivity;
import com.example.edulocker.activities.teacher.TeacherDashboardActivity;
import com.example.edulocker.databinding.ActivityConfirmationBinding;
import com.example.edulocker.models.User;

public class ConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityConfirmationBinding binding =
                ActivityConfirmationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String role = getIntent().getStringExtra("role");

        binding.btnContinue.setOnClickListener(v -> {
            Intent intent;
            switch (role != null ? role : "") {
                case User.ROLE_TEACHER: intent = new Intent(this, TeacherDashboardActivity.class); break;
                case User.ROLE_PARENT:  intent = new Intent(this, ParentDashboardActivity.class); break;
                default:                intent = new Intent(this, MainActivity.class); break;
            }
            startActivity(intent);
            finishAffinity();
        });

        binding.tvRestartApp.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }
}
