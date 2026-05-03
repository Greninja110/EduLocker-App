package com.example.edulocker.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.example.edulocker.R;
import com.example.edulocker.databinding.ActivityMainBinding;
import com.example.edulocker.fragments.DocumentsFragment;
import com.example.edulocker.fragments.HomeFragment;
import com.example.edulocker.fragments.NewsFragment;
import com.example.edulocker.fragments.ProfileFragment;
import com.example.edulocker.fragments.SearchFragment;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Dark status bar icons (visible on light pink-cream background)
        WindowInsetsControllerCompat insetsController =
                WindowCompat.getInsetsController(getWindow(), binding.getRoot());
        insetsController.setAppearanceLightStatusBars(true);

        // Push all fragment content below the status bar — one fix for every tab
        ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentContainer, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, statusBarHeight, 0, v.getPaddingBottom());
            return insets;
        });

        // Keep nav bar above system gesture bar without letting insets pad inside the pill
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNav, (v, insets) -> {
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            android.view.ViewGroup.MarginLayoutParams lp =
                    (android.view.ViewGroup.MarginLayoutParams) v.getLayoutParams();
            int baseDp20 = (int) (20 * getResources().getDisplayMetrics().density);
            lp.bottomMargin = baseDp20 + navBarHeight;
            v.setLayoutParams(lp);
            return WindowInsetsCompat.CONSUMED;
        });

        int startTab = getIntent().getIntExtra("nav_tab", R.id.nav_home);
        binding.bottomNav.setSelectedItemId(startTab);
        navigateTo(startTab);

        binding.bottomNav.setOnItemSelectedListener(item -> {
            navigateTo(item.getItemId());
            return true;
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int tab = intent.getIntExtra("nav_tab", R.id.nav_home);
        binding.bottomNav.setSelectedItemId(tab);
        navigateTo(tab);
    }

    private void navigateTo(int id) {
        if (id == R.id.nav_home) { loadFragment(new HomeFragment()); }
        else if (id == R.id.nav_search) { loadFragment(new SearchFragment()); }
        else if (id == R.id.nav_documents) { loadFragment(new DocumentsFragment()); }
        else if (id == R.id.nav_news) { loadFragment(new NewsFragment()); }
        else if (id == R.id.nav_profile) { loadFragment(new ProfileFragment()); }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        new SessionManager(this).clearSession();
        startActivity(new Intent(this, LoginActivity.class));
        finishAffinity();
    }
}
