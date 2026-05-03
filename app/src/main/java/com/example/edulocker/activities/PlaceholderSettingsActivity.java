package com.example.edulocker.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.edulocker.databinding.ActivityPlaceholderSettingsBinding;

public class PlaceholderSettingsActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "page_title";
    public static final String EXTRA_BODY  = "page_body";

    private ActivityPlaceholderSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaceholderSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, statusBarHeight, 0, 0);
            return insets;
        });

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String body  = getIntent().getStringExtra(EXTRA_BODY);

        if (title != null) binding.tvPageTitle.setText(title);
        if (body  != null) binding.tvPlaceholderBody.setText(body);

        binding.btnBack.setOnClickListener(v -> finish());
    }
}
