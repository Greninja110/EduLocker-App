package com.example.edulocker.activities.parent;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.edulocker.R;
import com.example.edulocker.databinding.ActivityParentDashboardBinding;
import com.example.edulocker.fragments.parent.ParentDocumentsFragment;
import com.example.edulocker.fragments.parent.ParentHomeFragment;
import com.example.edulocker.fragments.parent.ParentNoticesFragment;
import com.example.edulocker.fragments.parent.ParentProfileFragment;

public class ParentDashboardActivity extends AppCompatActivity {

    private ActivityParentDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityParentDashboardBinding.inflate(getLayoutInflater());
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
        if (id == R.id.nav_home) f = new ParentHomeFragment();
        else if (id == R.id.nav_notices) f = new ParentNoticesFragment();
        else if (id == R.id.nav_documents) f = new ParentDocumentsFragment();
        else if (id == R.id.nav_profile) f = new ParentProfileFragment();
        if (f != null) getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, f).commit();
    }
}
