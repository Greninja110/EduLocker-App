package com.example.edulocker.fragments.school;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.edulocker.activities.LoginActivity;
import com.example.edulocker.activities.SettingsActivity;
import com.example.edulocker.activities.school.ManageOrgActivity;
import com.example.edulocker.databinding.FragmentSchoolProfileBinding;
import com.example.edulocker.models.School;
import com.example.edulocker.repositories.SchoolRepository;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SchoolProfileFragment extends Fragment {

    private FragmentSchoolProfileBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSchoolProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SessionManager session = new SessionManager(requireContext());

        binding.tvUserName.setText(session.getUserName() != null ? session.getUserName() : "Org Admin");
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        binding.tvUserEmail.setText(u != null && u.getEmail() != null ? u.getEmail() : "");

        loadSchoolInfo(session.getSchoolId());

        binding.ivSettings.setOnClickListener(v ->
                startActivity(new Intent(getContext(), SettingsActivity.class)));

        binding.btnManageOrg.setOnClickListener(v ->
                startActivity(new Intent(getContext(), ManageOrgActivity.class)));

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            session.clearSession();
            startActivity(new Intent(getContext(), LoginActivity.class));
            requireActivity().finishAffinity();
        });

        binding.swipeRefresh.setColorSchemeResources(
                com.example.edulocker.R.color.primary, com.example.edulocker.R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(() -> loadSchoolInfo(session.getSchoolId()));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSchoolInfo(new SessionManager(requireContext()).getSchoolId());
    }

    private void loadSchoolInfo(String schoolId) {
        if (schoolId == null) return;
        if (binding != null) binding.progressBar.setVisibility(View.VISIBLE);

        new SchoolRepository().getSchool(schoolId, new SchoolRepository.SchoolCallback() {
            @Override
            public void onSuccess(School school) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    populateSchool(school);
                });
            }

            @Override
            public void onFailure(String error) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                });
            }
        });
    }

    private void populateSchool(School school) {
        binding.tvSchoolName.setText(school.getName());
        binding.tvSchoolId.setText(school.getSchoolId() != null ? school.getSchoolId() : "—");
        binding.tvSchoolCode.setText(school.getSchoolCode() != null ? school.getSchoolCode() : "—");
        binding.tvSchoolType.setText(school.getType() != null ? school.getType() : "—");
        binding.tvPrincipal.setText(school.getPrincipalName() != null ? school.getPrincipalName() : "Not set");
        String district = school.getDistrict() != null ? school.getDistrict() : "";
        String state = school.getState() != null ? ", " + school.getState() : "";
        binding.tvDistrict.setText(district + state);
        binding.tvAddress.setText(school.getAddress() != null ? school.getAddress() : "—");
        binding.tvPhone.setText(school.getPhone() != null ? school.getPhone() : "—");
        binding.tvStudentCount.setText(school.getStudentCount() + " students");
        binding.tvTeacherCount.setText(school.getTeacherCount() + " teachers");
        binding.cardOrgInfo.setVisibility(View.VISIBLE);
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
