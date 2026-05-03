package com.example.edulocker.fragments.govt;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.edulocker.activities.govt.SchoolDetailActivity;
import com.example.edulocker.adapters.SchoolAdapter;
import com.example.edulocker.databinding.FragmentGovtSchoolsBinding;
import com.example.edulocker.models.School;
import com.example.edulocker.repositories.SchoolRepository;
import com.example.edulocker.utils.Constants;
import com.google.firebase.firestore.AggregateSource;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GovtSchoolsFragment extends Fragment {

    private FragmentGovtSchoolsBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGovtSchoolsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvSchools.setLayoutManager(new LinearLayoutManager(getContext()));
        loadSchools();
    

        binding.swipeRefresh.setColorSchemeResources(
                com.example.edulocker.R.color.primary, com.example.edulocker.R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadSchools();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSchools();
    }

    private void loadSchools() {
        if (!isAdded() || binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        new SchoolRepository().getAllSchools(new SchoolRepository.SchoolListCallback() {
            @Override public void onSuccess(List<School> schools) {
                if (!isAdded() || binding == null) return;
                if (schools.isEmpty()) {
                    requireActivity().runOnUiThread(() -> {
                        if (binding == null) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        binding.rvSchools.setAdapter(new SchoolAdapter(schools, s -> {}));
                    });
                    return;
                }
                // Fetch real student counts from /students collection (not the cached field)
                AtomicInteger pending = new AtomicInteger(schools.size());
                for (School school : schools) {
                    Constants.db().collection(Constants.COL_STUDENTS)
                            .whereEqualTo("schoolId", school.getSchoolId())
                            .count()
                            .get(AggregateSource.SERVER)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    school.setStudentCount((int) task.getResult().getCount());
                                }
                                if (pending.decrementAndGet() == 0) {
                                    // All counts loaded — show list
                                    if (!isAdded() || binding == null) return;
                                    requireActivity().runOnUiThread(() -> {
                                        if (binding == null) return;
                                        binding.progressBar.setVisibility(View.GONE);
                                        binding.swipeRefresh.setRefreshing(false);
                                        binding.rvSchools.setAdapter(new SchoolAdapter(schools, school2 -> {
                                            Intent intent = new Intent(getContext(),
                                                    SchoolDetailActivity.class);
                                            intent.putExtra(Constants.EXTRA_SCHOOL_ID,
                                                    school2.getSchoolId());
                                            startActivity(intent);
                                        }));
                                    });
                                }
                            });
                }
            }
            @Override public void onFailure(String error) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    android.widget.Toast.makeText(requireContext(),
                            "Failed to load organisations: " + error,
                            android.widget.Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
