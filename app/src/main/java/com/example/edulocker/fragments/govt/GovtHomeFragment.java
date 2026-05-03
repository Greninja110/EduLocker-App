package com.example.edulocker.fragments.govt;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.edulocker.activities.govt.RegisterSchoolActivity;
import com.example.edulocker.databinding.FragmentGovtHomeBinding;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.firestore.AggregateSource;

import java.util.Calendar;

public class GovtHomeFragment extends Fragment {

    private FragmentGovtHomeBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGovtHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SessionManager session = new SessionManager(requireContext());
        String name = session.getUserName();
        binding.tvGovtName.setText((name != null ? name : "Government Admin") + " !!");
        binding.tvTimeGreeting.setText(getGreeting());

        loadStats();
        binding.btnRegisterSchool.setOnClickListener(v ->
                startActivity(new Intent(getContext(), RegisterSchoolActivity.class)));

        binding.swipeRefresh.setColorSchemeResources(
                com.example.edulocker.R.color.primary, com.example.edulocker.R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadStats();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        if (!isAdded() || binding == null) return;
        Constants.db().collection(Constants.COL_SCHOOLS).count().get(AggregateSource.SERVER)
                .addOnSuccessListener(snap -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.swipeRefresh.setRefreshing(false);
                        if (binding != null) binding.tvSchoolCount.setText(String.valueOf(snap.getCount()));
                    });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.swipeRefresh.setRefreshing(false);
                        if (binding != null) binding.tvSchoolCount.setText("—");
                    });
                });

        Constants.db().collection(Constants.COL_STUDENTS).count().get(AggregateSource.SERVER)
                .addOnSuccessListener(snap -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.tvStudentCount.setText(String.valueOf(snap.getCount()));
                    });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.tvStudentCount.setText("—");
                    });
                });

        Constants.db().collection(Constants.COL_DOCUMENTS).count().get(AggregateSource.SERVER)
                .addOnSuccessListener(snap -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.tvDocCount.setText(String.valueOf(snap.getCount()));
                    });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.tvDocCount.setText("—");
                    });
                });

        Constants.db().collection(Constants.COL_TEACHERS).count().get(AggregateSource.SERVER)
                .addOnSuccessListener(snap -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.tvFacultyCount.setText(String.valueOf(snap.getCount()));
                    });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.tvFacultyCount.setText("—");
                    });
                });

        Constants.db().collection(Constants.COL_SCHOLARSHIPS).count().get(AggregateSource.SERVER)
                .addOnSuccessListener(snap -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.tvScholarshipCount.setText(String.valueOf(snap.getCount()));
                    });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.tvScholarshipCount.setText("—");
                    });
                });

        Constants.db().collection(Constants.COL_NOTICES).count().get(AggregateSource.SERVER)
                .addOnSuccessListener(snap -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.tvNoticeCount.setText(String.valueOf(snap.getCount()));
                    });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.tvNoticeCount.setText("—");
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
