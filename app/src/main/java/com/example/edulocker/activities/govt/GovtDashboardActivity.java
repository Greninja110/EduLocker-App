package com.example.edulocker.activities.govt;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.edulocker.R;
import com.example.edulocker.databinding.ActivityGovtDashboardBinding;
import com.example.edulocker.fragments.govt.GovtHomeFragment;
import com.example.edulocker.fragments.govt.GovtNoticesFragment;
import com.example.edulocker.fragments.govt.GovtProfileFragment;
import com.example.edulocker.fragments.govt.GovtSchoolsFragment;

public class GovtDashboardActivity extends AppCompatActivity {

    private ActivityGovtDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGovtDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);

        ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentContainer, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, top, 0, v.getPaddingBottom());
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNav, (v, insets) -> {
            int nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.bottomMargin = (int) (20 * getResources().getDisplayMetrics().density) + nav;
            v.setLayoutParams(lp);
            return WindowInsetsCompat.CONSUMED;
        });

        navigateTo(R.id.nav_home);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            navigateTo(item.getItemId());
            return true;
        });
    }

    private void navigateTo(int id) {
        Fragment f = null;
        if (id == R.id.nav_home) f = new GovtHomeFragment();
        else if (id == R.id.nav_notices) f = new GovtNoticesFragment();
        else if (id == R.id.nav_schools) f = new GovtSchoolsFragment();
        else if (id == R.id.nav_profile) f = new GovtProfileFragment();
        if (f != null) getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, f).commit();
    }
}
