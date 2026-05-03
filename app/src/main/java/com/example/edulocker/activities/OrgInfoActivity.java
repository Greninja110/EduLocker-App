package com.example.edulocker.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.edulocker.adapters.VacancyAdapter;
import com.example.edulocker.databinding.ActivityOrgInfoBinding;
import com.example.edulocker.models.Vacancy;
import com.example.edulocker.repositories.SchoolRepository;
import com.example.edulocker.repositories.VacancyRepository;

import java.util.ArrayList;
import java.util.List;

public class OrgInfoActivity extends AppCompatActivity {

    public static final String EXTRA_SCHOOL_ID = "extra_school_id";

    private ActivityOrgInfoBinding binding;
    private final List<Vacancy> vacancies = new ArrayList<>();
    private VacancyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrgInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, top, 0, 0);
            return insets;
        });
        binding.btnBack.setOnClickListener(v -> finish());

        String schoolId = getIntent().getStringExtra(EXTRA_SCHOOL_ID);
        if (schoolId == null) { finish(); return; }

        // Read-only vacancy list — no admin actions
        adapter = new VacancyAdapter(vacancies, false, v -> {}, v -> {});
        binding.rvVacancies.setLayoutManager(new LinearLayoutManager(this));
        binding.rvVacancies.setAdapter(adapter);

        loadSchool(schoolId);
        loadVacancies(schoolId);
    }

    private void loadSchool(String schoolId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        new SchoolRepository().getSchool(schoolId, new SchoolRepository.SchoolCallback() {
            @Override
            public void onSuccess(com.example.edulocker.models.School school) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvSchoolName.setText(school.getName());
                    binding.tvType.setText(school.getType() != null
                            ? school.getType().replace("_", " ") : "");
                    binding.tvPrincipal.setText(school.getPrincipalName() != null
                            ? school.getPrincipalName() : "N/A");
                    binding.tvDistrict.setText(
                            (school.getDistrict() != null ? school.getDistrict() : "")
                            + ", " + (school.getState() != null ? school.getState() : ""));
                    binding.tvAddress.setText(school.getAddress() != null
                            ? school.getAddress() : "N/A");
                    binding.tvPhone.setText(school.getPhone() != null
                            ? school.getPhone() : "N/A");
                    binding.tvStudentCount.setText(school.getStudentCount() + " students • "
                            + school.getTeacherCount() + " teachers");
                });
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> binding.progressBar.setVisibility(View.GONE));
            }
        });
    }

    private void loadVacancies(String schoolId) {
        new VacancyRepository().getVacancies(schoolId, new VacancyRepository.VacancyListCallback() {
            @Override
            public void onSuccess(List<Vacancy> list) {
                runOnUiThread(() -> {
                    vacancies.clear();
                    vacancies.addAll(list);
                    adapter.notifyDataSetChanged();
                    binding.tvNoVacancies.setVisibility(
                            vacancies.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }
            @Override
            public void onFailure(String error) {}
        });
    }
}
