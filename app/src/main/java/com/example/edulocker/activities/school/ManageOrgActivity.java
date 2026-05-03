package com.example.edulocker.activities.school;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.edulocker.adapters.VacancyAdapter;
import com.example.edulocker.databinding.ActivityManageOrgBinding;
import com.example.edulocker.models.School;
import com.example.edulocker.models.Vacancy;
import com.example.edulocker.repositories.SchoolRepository;
import com.example.edulocker.repositories.VacancyRepository;
import com.example.edulocker.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ManageOrgActivity extends AppCompatActivity {

    private ActivityManageOrgBinding binding;
    private VacancyRepository vacancyRepo;
    private SessionManager session;
    private String schoolId;
    private School currentSchool;
    private final List<Vacancy> vacancies = new ArrayList<>();
    private VacancyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageOrgBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, top, 0, 0);
            return insets;
        });

        session = new SessionManager(this);
        vacancyRepo = new VacancyRepository();
        schoolId = session.getSchoolId();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.fabAddVacancy.setOnClickListener(v -> showVacancyDialog(null));

        adapter = new VacancyAdapter(vacancies, true,
                vacancy -> showVacancyDialog(vacancy),
                vacancy -> confirmDelete(vacancy));
        binding.rvVacancies.setLayoutManager(new LinearLayoutManager(this));
        binding.rvVacancies.setAdapter(adapter);

        loadSchool();
        loadVacancies();
    }

    private void loadSchool() {
        new SchoolRepository().getSchool(schoolId, new SchoolRepository.SchoolCallback() {
            @Override
            public void onSuccess(School school) {
                currentSchool = school;
                runOnUiThread(() -> {
                    binding.tvSchoolName.setText(school.getName());
                    binding.tvPrincipal.setText(school.getPrincipalName() != null
                            ? school.getPrincipalName() : "Not set");
                    binding.tvDistrict.setText(
                            (school.getDistrict() != null ? school.getDistrict() : "")
                            + ", " + (school.getState() != null ? school.getState() : ""));
                    binding.tvAddress.setText(school.getAddress() != null ? school.getAddress() : "Not set");
                    binding.tvType.setText(school.getType() != null
                            ? school.getType().replace("_", " ") : "");
                    binding.tvStudentCount.setText(school.getStudentCount() + " students • "
                            + school.getTeacherCount() + " teachers");
                });
            }
            @Override
            public void onFailure(String error) {}
        });
    }

    private void loadVacancies() {
        binding.progressBar.setVisibility(View.VISIBLE);
        vacancyRepo.getVacancies(schoolId, new VacancyRepository.VacancyListCallback() {
            @Override
            public void onSuccess(List<Vacancy> list) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    vacancies.clear();
                    vacancies.addAll(list);
                    adapter.notifyDataSetChanged();
                    binding.tvNoVacancies.setVisibility(vacancies.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> binding.progressBar.setVisibility(View.GONE));
            }
        });
    }

    private void showVacancyDialog(Vacancy existing) {
        View dialogView = LayoutInflater.from(this)
                .inflate(com.example.edulocker.R.layout.dialog_add_vacancy, null);

        TextInputEditText etClass  = dialogView.findViewById(com.example.edulocker.R.id.et_class_stream);
        TextInputEditText etSeats  = dialogView.findViewById(com.example.edulocker.R.id.et_seats);

        if (existing != null) {
            etClass.setText(existing.getClassStream());
            etSeats.setText(String.valueOf(existing.getSeats()));
        }

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Add Vacancy" : "Edit Vacancy")
                .setView(dialogView)
                .setPositiveButton("Save", (d, w) -> {
                    String classStream = etClass.getText() != null
                            ? etClass.getText().toString().trim() : "";
                    if (classStream.isEmpty()) {
                        Toast.makeText(this, "Enter class / stream", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Vacancy v = existing != null ? existing : new Vacancy();
                    v.setClassStream(classStream);
                    try {
                        v.setSeats(Integer.parseInt(
                                etSeats.getText() != null ? etSeats.getText().toString().trim() : "0"));
                    } catch (NumberFormatException e) { v.setSeats(0); }

                    VacancyRepository.VacancyCallback cb = new VacancyRepository.VacancyCallback() {
                        @Override public void onSuccess() { runOnUiThread(() -> loadVacancies()); }
                        @Override public void onFailure(String e) {
                            runOnUiThread(() ->
                                    Toast.makeText(ManageOrgActivity.this, "Error: " + e,
                                            Toast.LENGTH_SHORT).show());
                        }
                    };

                    if (existing == null) vacancyRepo.addVacancy(schoolId, v, cb);
                    else vacancyRepo.updateVacancy(schoolId, v, cb);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(Vacancy vacancy) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Vacancy")
                .setMessage("Delete vacancy for " + vacancy.getClassStream() + "?")
                .setPositiveButton("Delete", (d, w) ->
                        vacancyRepo.deleteVacancy(schoolId, vacancy.getVacancyId(),
                                new VacancyRepository.VacancyCallback() {
                                    @Override public void onSuccess() {
                                        runOnUiThread(() -> loadVacancies());
                                    }
                                    @Override public void onFailure(String e) {}
                                }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
