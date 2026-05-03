package com.example.edulocker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edulocker.activities.parent.ParentDashboardActivity;
import com.example.edulocker.activities.teacher.TeacherDashboardActivity;
import com.example.edulocker.databinding.ActivityProfileCreationLoadingBinding;
import com.example.edulocker.models.User;

public class ProfileCreationLoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityProfileCreationLoadingBinding binding =
                ActivityProfileCreationLoadingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(this, ConfirmationActivity.class);
            intent.putExtra("role", getIntent().getStringExtra("role"));
            startActivity(intent);
            finish();
        }, 600);
    }
}
