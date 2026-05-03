package com.example.edulocker.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.edulocker.databinding.ActivitySettingsBinding;
import com.example.edulocker.utils.AccountManager;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        session = new SessionManager(this);

        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, statusBarHeight, 0, 0);
            return insets;
        });

        binding.btnBack.setOnClickListener(v -> finish());

        binding.tvUserName.setText(session.getUserName());
        binding.tvUserRole.setText(capitalize(session.getUserRole()) + " Account");

        binding.rowAccountDetails.setOnClickListener(v ->
                startActivity(new Intent(this, AccountDetailsActivity.class)));

        binding.rowAccounts.setOnClickListener(v ->
                startActivity(new Intent(this, AccountSwitcherActivity.class)));

        binding.rowPrivacy.setOnClickListener(v ->
                openPlaceholder("Privacy Settings",
                        "This is the Privacy Settings page.\n\nNo information available for prototype app."));
        binding.rowPersonalization.setOnClickListener(v ->
                openPlaceholder("Personalization & Display",
                        "This is the Personalization & Display page.\n\nNo information available for prototype app."));
        binding.rowAccessibility.setOnClickListener(v ->
                openPlaceholder("Accessibility",
                        "This is the Accessibility page.\n\nNo information available for prototype app."));
        binding.rowContentPrivacy.setOnClickListener(v ->
                openPlaceholder("Content Privacy",
                        "This is the Content Privacy page.\n\nNo information available for prototype app."));
        binding.rowPermissions.setOnClickListener(v ->
                openPlaceholder("App Permissions",
                        "This is the App Permissions page.\n\nNo information available for prototype app."));
        binding.rowParentControl.setOnClickListener(v ->
                openPlaceholder("Parent Control & Tools",
                        "This is the Parent Control & Tools page.\n\nNo information available for prototype app."));
        binding.rowHelp.setOnClickListener(v ->
                openPlaceholder("Help & Support",
                        "This is the Help & Support page.\n\nNo information available for prototype app."));
        binding.rowPolicy.setOnClickListener(v ->
                openPlaceholder("Privacy Policy incl. T&C",
                        "This is the Privacy Policy page.\n\nNo information available for prototype app."));

        binding.tvLogout.setOnClickListener(v -> confirmLogout());
        updateAccountCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAccountCount();
    }

    private void updateAccountCount() {
        int count = AccountManager.getAccounts(this).size();
        binding.tvAccountCount.setText(count + " saved account" + (count == 1 ? "" : "s"));
    }

    private void openPlaceholder(String title, String body) {
        Intent intent = new Intent(this, PlaceholderSettingsActivity.class);
        intent.putExtra(PlaceholderSettingsActivity.EXTRA_TITLE, title);
        intent.putExtra(PlaceholderSettingsActivity.EXTRA_BODY, body);
        startActivity(intent);
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (d, w) -> {
                    FirebaseAuth.getInstance().signOut();
                    session.clearSession();
                    startActivity(new Intent(this, LoginActivity.class));
                    finishAffinity();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
