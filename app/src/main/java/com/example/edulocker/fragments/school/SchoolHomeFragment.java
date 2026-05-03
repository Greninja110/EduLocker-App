package com.example.edulocker.fragments.school;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.edulocker.activities.school.AddStudentActivity;
import com.example.edulocker.activities.school.AddTeacherActivity;
import com.example.edulocker.databinding.FragmentSchoolHomeBinding;
import com.example.edulocker.models.School;
import com.example.edulocker.repositories.SchoolRepository;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.firestore.AggregateSource;

import java.util.Calendar;

public class SchoolHomeFragment extends Fragment {

    private FragmentSchoolHomeBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSchoolHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvTimeGreeting.setText(getGreeting());
        loadData();
        binding.btnAddStudent.setOnClickListener(v ->
                startActivity(new Intent(getContext(), AddStudentActivity.class)));
        binding.btnAddTeacher.setOnClickListener(v ->
                startActivity(new Intent(getContext(), AddTeacherActivity.class)));

        binding.swipeRefresh.setColorSchemeResources(
                com.example.edulocker.R.color.primary, com.example.edulocker.R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(this::loadData);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        String schoolId = new SessionManager(requireContext()).getSchoolId();
        if (schoolId == null) return;

        loadSchoolInfo(schoolId);
        loadStats(schoolId);
    }

    private void loadSchoolInfo(String schoolId) {
        new SchoolRepository().getSchool(schoolId, new SchoolRepository.SchoolCallback() {
            @Override public void onSuccess(School school) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.swipeRefresh.setRefreshing(false);
                    binding.tvSchoolName.setText(school.getName() + " !!");
                    binding.tvDistrict.setText(school.getDistrict());
                });
            }
            @Override public void onFailure(String error) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.swipeRefresh.setRefreshing(false);
                });
            }
        });
    }

    private void loadStats(String schoolId) {
        Constants.db().collection(Constants.COL_STUDENTS)
                .whereEqualTo("schoolId", schoolId)
                .count().get(AggregateSource.SERVER)
                .addOnSuccessListener(snap -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null)
                            binding.tvStudentCount.setText(String.valueOf(snap.getCount()));
                    });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.tvStudentCount.setText("—");
                    });
                });

        Constants.db().collection(Constants.COL_TEACHERS)
                .whereEqualTo("schoolId", schoolId)
                .count().get(AggregateSource.SERVER)
                .addOnSuccessListener(snap -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null)
                            binding.tvTeacherCount.setText(String.valueOf(snap.getCount()));
                    });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.tvTeacherCount.setText("—");
                    });
                });

        Constants.db().collection(Constants.COL_DOCUMENTS)
                .whereEqualTo("schoolId", schoolId)
                .count().get(AggregateSource.SERVER)
                .addOnSuccessListener(snap -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null)
                            binding.tvDocCount.setText(String.valueOf(snap.getCount()));
                    });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.tvDocCount.setText("—");
                    });
                });
    }

    private String getGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        if (hour < 21) return "Good Evening";
        return "Good Night";
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
