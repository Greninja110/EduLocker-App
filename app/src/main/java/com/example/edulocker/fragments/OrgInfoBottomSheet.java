package com.example.edulocker.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.edulocker.adapters.VacancyAdapter;
import com.example.edulocker.databinding.BottomSheetOrgInfoBinding;
import com.example.edulocker.models.School;
import com.example.edulocker.models.User;
import com.example.edulocker.models.Vacancy;
import com.example.edulocker.repositories.VacancyRepository;
import com.example.edulocker.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class OrgInfoBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_SCHOOL_ID = "arg_school_id";

    private BottomSheetOrgInfoBinding binding;
    private School school;
    private String schoolId;
    private final List<Vacancy> vacancies = new ArrayList<>();
    private VacancyAdapter vacancyAdapter;
    private VacancyRepository vacancyRepo;
    private boolean isSchoolAdmin = false;

    public static OrgInfoBottomSheet newInstance(School school) {
        OrgInfoBottomSheet sheet = new OrgInfoBottomSheet();
        Bundle args = new Bundle();
        // Pass only the school ID to avoid Timestamp serialization issues;
        // we store the school object directly via tag after creation
        args.putString(ARG_SCHOOL_ID, school.getSchoolId());
        sheet.setArguments(args);
        sheet.school = school; // held in memory for this session
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            schoolId = getArguments().getString(ARG_SCHOOL_ID);
        }
        vacancyRepo = new VacancyRepository();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new BottomSheetDialog(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetOrgInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager session = new SessionManager(requireContext());
        isSchoolAdmin = User.ROLE_SCHOOL.equals(session.getUserRole())
                && school != null && school.getSchoolId().equals(session.getSchoolId());

        populateSchoolInfo();

        vacancyAdapter = new VacancyAdapter(vacancies, isSchoolAdmin,
                vacancy -> onEditVacancy(vacancy),
                vacancy -> onDeleteVacancy(vacancy));
        binding.rvVacancies.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvVacancies.setAdapter(vacancyAdapter);

        if (isSchoolAdmin) {
            binding.btnAddVacancy.setVisibility(View.VISIBLE);
            binding.btnAddVacancy.setOnClickListener(v -> showAddVacancyDialog(null));
        } else {
            binding.btnAddVacancy.setVisibility(View.GONE);
        }

        if (school != null) {
            loadVacancies();
        } else if (schoolId != null) {
            // Fallback: fetch school from Firestore if not in memory
            com.example.edulocker.utils.Constants.db()
                    .collection(com.example.edulocker.utils.Constants.COL_SCHOOLS)
                    .document(schoolId).get()
                    .addOnSuccessListener(doc -> {
                        school = doc.toObject(School.class);
                        if (school != null && isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                populateSchoolInfo();
                                loadVacancies();
                            });
                        }
                    });
        }
    }

    private void populateSchoolInfo() {
        if (school == null) return;
        binding.tvSchoolName.setText(school.getName());
        binding.tvPrincipal.setText("Principal: " + (school.getPrincipalName() != null
                ? school.getPrincipalName() : "N/A"));
        binding.tvType.setText(school.getType() != null
                ? school.getType().replace("_", " ") : "");
        binding.tvDistrict.setText(school.getDistrict() + ", " + school.getState());
        binding.tvAddress.setText(school.getAddress() != null ? school.getAddress() : "");
        binding.tvPhone.setText(school.getPhone() != null ? school.getPhone() : "");
        binding.tvStudentCount.setText(school.getStudentCount() + " students");
    }

    private void loadVacancies() {
        vacancyRepo.getVacancies(school.getSchoolId(), new VacancyRepository.VacancyListCallback() {
            @Override
            public void onSuccess(List<Vacancy> list) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    vacancies.clear();
                    vacancies.addAll(list);
                    vacancyAdapter.notifyDataSetChanged();
                    binding.tvNoVacancies.setVisibility(vacancies.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }
            @Override
            public void onFailure(String error) {}
        });
    }

    private void showAddVacancyDialog(@Nullable Vacancy existing) {
        View v = LayoutInflater.from(getContext()).inflate(
                com.example.edulocker.R.layout.dialog_add_vacancy, null);

        com.google.android.material.textfield.TextInputEditText etClass =
                v.findViewById(com.example.edulocker.R.id.et_class_stream);
        com.google.android.material.textfield.TextInputEditText etSeats =
                v.findViewById(com.example.edulocker.R.id.et_seats);
        if (existing != null) {
            etClass.setText(existing.getClassStream());
            etSeats.setText(String.valueOf(existing.getSeats()));
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Add Vacancy" : "Edit Vacancy")
                .setView(v)
                .setPositiveButton("Save", (dialog, w) -> {
                    String classStream = etClass.getText() != null
                            ? etClass.getText().toString().trim() : "";
                    String seatsStr = etSeats.getText() != null
                            ? etSeats.getText().toString().trim() : "0";
                    if (classStream.isEmpty()) {
                        Toast.makeText(getContext(), "Enter class/stream", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Vacancy vacancy = existing != null ? existing : new Vacancy();
                    vacancy.setClassStream(classStream);
                    try { vacancy.setSeats(Integer.parseInt(seatsStr)); }
                    catch (NumberFormatException e) { vacancy.setSeats(0); }

                    if (existing == null) {
                        vacancyRepo.addVacancy(school.getSchoolId(), vacancy,
                                new VacancyRepository.VacancyCallback() {
                                    @Override public void onSuccess() {
                                        if (isAdded()) requireActivity().runOnUiThread(() -> loadVacancies());
                                    }
                                    @Override public void onFailure(String e) {}
                                });
                    } else {
                        vacancyRepo.updateVacancy(school.getSchoolId(), vacancy,
                                new VacancyRepository.VacancyCallback() {
                                    @Override public void onSuccess() {
                                        if (isAdded()) requireActivity().runOnUiThread(() -> loadVacancies());
                                    }
                                    @Override public void onFailure(String e) {}
                                });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void onEditVacancy(Vacancy vacancy) {
        showAddVacancyDialog(vacancy);
    }

    private void onDeleteVacancy(Vacancy vacancy) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Vacancy")
                .setMessage("Delete vacancy for " + vacancy.getClassStream() + "?")
                .setPositiveButton("Delete", (d, w) ->
                        vacancyRepo.deleteVacancy(school.getSchoolId(), vacancy.getVacancyId(),
                                new VacancyRepository.VacancyCallback() {
                                    @Override public void onSuccess() {
                                        if (isAdded()) requireActivity().runOnUiThread(() -> loadVacancies());
                                    }
                                    @Override public void onFailure(String e) {}
                                }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
