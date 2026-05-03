package com.example.edulocker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.edulocker.activities.ScholarshipApplicationActivity;
import com.example.edulocker.adapters.ApplicationStatusAdapter;
import com.example.edulocker.adapters.NoticeAdapter;
import com.example.edulocker.adapters.ScholarshipAdapter;
import com.example.edulocker.databinding.FragmentHomeBinding;
import com.example.edulocker.models.Notice;
import com.example.edulocker.models.Scholarship;
import com.example.edulocker.models.ScholarshipApplication;
import com.example.edulocker.models.Student;
import com.example.edulocker.repositories.NoticeRepository;
import com.example.edulocker.repositories.ScholarshipRepository;
import com.example.edulocker.repositories.StudentRepository;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.SessionManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = new SessionManager(requireContext());
        binding.rvNotices.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNotices.setNestedScrollingEnabled(false);
        loadStudentData();
    

        binding.swipeRefresh.setColorSchemeResources(
                com.example.edulocker.R.color.primary, com.example.edulocker.R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadStudentData();
        });
    }

    private void loadStudentData() {
        String passportId = session.getPassportId();
        if (passportId == null) return;

        new StudentRepository().getStudent(passportId, new StudentRepository.StudentCallback() {
            @Override
            public void onSuccess(Student student) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding != null) binding.swipeRefresh.setRefreshing(false);
                    if (binding == null) return;
                    populateHeader(student);
                });
                loadEligibleScholarships(student);
                loadNotices(student);
                loadMyApplications(student.getPassportId());
            }

            @Override
            public void onFailure(String error) {}
        });
    }

    private void populateHeader(Student student) {
        String greeting = getGreeting();
        binding.tvGreeting.setText("Hii");
        binding.tvStudentName.setText(student.getName() + " !!");
        binding.tvTimeGreeting.setText(greeting);
        binding.tvSchoolName.setText(student.getSchoolName());
        binding.tvAttendance.setText("AGR " + student.getAttendancePercentage());
    }

    private void loadEligibleScholarships(Student student) {
        new ScholarshipRepository().getEligibleScholarships(student,
                new ScholarshipRepository.ScholarshipListCallback() {
                    @Override
                    public void onSuccess(List<Scholarship> scholarships) {
                        if (!isAdded() || binding == null) return;
                        requireActivity().runOnUiThread(() -> {
                            if (binding == null) return;
                            if (!scholarships.isEmpty()) {
                                binding.tvScholarshipAlert.setVisibility(View.VISIBLE);
                                binding.cardScholarship.setVisibility(View.VISIBLE);
                                Scholarship first = scholarships.get(0);
                                binding.tvScholarshipTitle.setText(first.getTitle());
                                binding.tvScholarshipEligible.setText(
                                        "You are eligible for this Scholarship, Click here to apply");
                                binding.cardScholarship.setOnClickListener(v -> {
                                    Intent intent = new Intent(getContext(),
                                            ScholarshipApplicationActivity.class);
                                    intent.putExtra(Constants.EXTRA_SCHOLARSHIP_ID,
                                            first.getScholarshipId());
                                    startActivity(intent);
                                });
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {}
                });
    }

    private void loadNotices(Student student) {
        if (!isAdded() || binding == null) return;
        requireActivity().runOnUiThread(() -> {
            if (binding == null) return;
            binding.progressNotices.setVisibility(View.VISIBLE);
        });
        new NoticeRepository().getStudentNotices(
                student.getSchoolId(), student.getState(),
                new NoticeRepository.NoticeListCallback() {
                    @Override
                    public void onSuccess(List<Notice> notices) {
                        if (!isAdded() || binding == null) return;
                        requireActivity().runOnUiThread(() -> {
                            if (binding == null) return;
                            binding.progressNotices.setVisibility(View.GONE);
                            if (notices.isEmpty()) {
                                binding.tvNoNotices.setVisibility(View.VISIBLE);
                            } else {
                                binding.tvNoNotices.setVisibility(View.GONE);
                                binding.rvNotices.setAdapter(new NoticeAdapter(notices));
                            }
                        });
                    }
                    @Override
                    public void onFailure(String error) {
                        if (!isAdded() || binding == null) return;
                        requireActivity().runOnUiThread(() -> {
                            if (binding == null) return;
                            binding.progressNotices.setVisibility(View.GONE);
                            binding.tvNoNotices.setVisibility(View.VISIBLE);
                        });
                    }
                });
    }

    private void loadMyApplications(String passportId) {
        new ScholarshipRepository().getApplicationsForStudent(passportId,
                new ScholarshipRepository.ApplicationListCallback() {
                    @Override
                    public void onSuccess(List<ScholarshipApplication> applications) {
                        if (!isAdded() || binding == null) return;
                        requireActivity().runOnUiThread(() -> {
                            if (binding == null || applications.isEmpty()) return;
                            binding.tvMyApplicationsLabel.setVisibility(View.VISIBLE);
                            binding.rvMyApplications.setVisibility(View.VISIBLE);
                            binding.rvMyApplications.setLayoutManager(
                                    new LinearLayoutManager(getContext()));
                            binding.rvMyApplications.setAdapter(
                                    new ApplicationStatusAdapter(applications));
                        });
                    }
                    @Override
                    public void onFailure(String error) {}
                });
    }

    private String getGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        if (hour < 21) return "Good Evening";
        return "Good Night";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
