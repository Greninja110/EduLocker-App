package com.example.edulocker.activities.govt;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.edulocker.databinding.ActivitySchoolDetailBinding;
import com.example.edulocker.models.School;
import com.example.edulocker.repositories.SchoolRepository;
import com.example.edulocker.utils.Constants;

public class SchoolDetailActivity extends AppCompatActivity {

    private ActivitySchoolDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySchoolDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, statusBarHeight, 0, 0);
            return insets;
        });

        binding.btnBack.setOnClickListener(v -> finish());

        String schoolId = getIntent().getStringExtra(Constants.EXTRA_SCHOOL_ID);
        if (schoolId != null) loadSchool(schoolId);
    }

    private void loadSchool(String schoolId) {
        new SchoolRepository().getSchool(schoolId, new SchoolRepository.SchoolCallback() {
            @Override
            public void onSuccess(School school) {
                runOnUiThread(() -> {
                    binding.tvToolbarTitle.setText(school.getName());
                    binding.tvSchoolName.setText(school.getName());

                    String district = school.getDistrict() != null ? school.getDistrict() : "—";
                    String districtCode = school.getDistrictCode() != null
                            ? " (" + school.getDistrictCode() + ")" : "";
                    binding.tvDistrict.setText(district + districtCode);

                    binding.tvType.setText(school.getType() != null ? school.getType() : "—");
                    binding.tvSchoolCode.setText(school.getSchoolCode() != null ? school.getSchoolCode() : "—");
                    binding.tvEmail.setText(school.getLoginEmail() != null ? school.getLoginEmail() : "—");
                    binding.tvPrincipal.setText(school.getPrincipalName() != null ? school.getPrincipalName() : "—");
                    binding.tvAddress.setText(school.getAddress() != null ? school.getAddress() : "—");
                    binding.tvStudentCount.setText(school.getStudentCount() + " students");
                    binding.tvTeacherCount.setText(school.getTeacherCount() + " teachers");
                });
            }

            @Override
            public void onFailure(String error) {}
        });
    }
}
