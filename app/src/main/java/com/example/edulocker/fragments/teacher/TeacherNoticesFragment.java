package com.example.edulocker.fragments.teacher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.edulocker.R;
import com.example.edulocker.adapters.NoticeAdapter;
import com.example.edulocker.adapters.ScholarshipAdapter;
import com.example.edulocker.databinding.FragmentTeacherNoticesBinding;
import com.example.edulocker.models.Notice;
import com.example.edulocker.models.Scholarship;
import com.example.edulocker.repositories.NoticeRepository;
import com.example.edulocker.repositories.ScholarshipRepository;
import com.example.edulocker.utils.SessionManager;

import java.util.List;

public class TeacherNoticesFragment extends Fragment {

    private FragmentTeacherNoticesBinding binding;
    private boolean showingScholarships = true;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTeacherNoticesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvNotices.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvScholarships.setLayoutManager(new LinearLayoutManager(getContext()));

        showScholarships();

        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            if (ids.get(0) == R.id.chip_scholarships) showScholarships();
            else showNotices();
        });

        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (showingScholarships) loadScholarships(); else loadNotices();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null) return;
        if (showingScholarships) loadScholarships(); else loadNotices();
    }

    private void showScholarships() {
        if (binding == null) return;
        showingScholarships = true;
        binding.sectionScholarships.setVisibility(View.VISIBLE);
        binding.sectionNotices.setVisibility(View.GONE);
        loadScholarships();
    }

    private void showNotices() {
        if (binding == null) return;
        showingScholarships = false;
        binding.sectionScholarships.setVisibility(View.GONE);
        binding.sectionNotices.setVisibility(View.VISIBLE);
        loadNotices();
    }

    private void loadScholarships() {
        if (!isAdded() || binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        new ScholarshipRepository().getAllScholarships(new ScholarshipRepository.ScholarshipListCallback() {
            @Override public void onSuccess(List<Scholarship> list) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    binding.rvScholarships.setAdapter(new ScholarshipAdapter(list, s -> {}));
                });
            }
            @Override public void onFailure(String e) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                });
            }
        });
    }

    private void loadNotices() {
        if (!isAdded() || binding == null) return;
        String schoolId = new SessionManager(requireContext()).getSchoolId();
        if (schoolId == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        new NoticeRepository().getFacultyNotices(schoolId, new NoticeRepository.NoticeListCallback() {
            @Override public void onSuccess(List<Notice> notices) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    binding.tvEmpty.setVisibility(notices.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.rvNotices.setAdapter(new NoticeAdapter(notices));
                });
            }
            @Override public void onFailure(String error) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                });
            }
        });
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
