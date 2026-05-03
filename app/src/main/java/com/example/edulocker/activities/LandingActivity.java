package com.example.edulocker.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edulocker.databinding.ActivityLandingBinding;

public class LandingActivity extends AppCompatActivity {

    private ActivityLandingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLandingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        binding.btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterStep1Activity.class)));

        binding.tvRescueAccount.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        // Hidden entry: long-press "rescue my account" → demo setup screen
        binding.tvRescueAccount.setOnLongClickListener(v -> {
            startActivity(new Intent(this, SeedDataActivity.class));
            return true;
        });
    }
}
