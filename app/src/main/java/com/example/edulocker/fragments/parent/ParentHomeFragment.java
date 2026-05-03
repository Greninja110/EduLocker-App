package com.example.edulocker.fragments.parent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.edulocker.databinding.FragmentParentHomeBinding;
import com.example.edulocker.models.Student;
import com.example.edulocker.repositories.StudentRepository;
import com.example.edulocker.utils.SessionManager;

import java.util.Calendar;

public class ParentHomeFragment extends Fragment {

    private FragmentParentHomeBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentParentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager session = new SessionManager(requireContext());
        String name = session.getUserName();
        binding.tvParentName.setText((name != null ? name : "Parent") + " !!");
        binding.tvTimeGreeting.setText(getGreeting());

        loadChild();

        binding.swipeRefresh.setColorSchemeResources(
                com.example.edulocker.R.color.primary, com.example.edulocker.R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(this::loadChild);
    }

    private void loadChild() {
        String uid = new SessionManager(requireContext()).getUserUid();
        if (uid == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.cardChild.setVisibility(View.GONE);

        new StudentRepository().getStudentByParentUserId(uid, new StudentRepository.StudentCallback() {
            @Override public void onSuccess(Student student) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    binding.cardChild.setVisibility(View.VISIBLE);
                    binding.tvChildName.setText(student.getName());
                    binding.tvPassportId.setText("Passport ID: " + student.getPassportId());
                    binding.tvSchool.setText(student.getSchoolName());
                    binding.tvClass.setText("Class " + student.getStudentClass());
                    binding.tvAttendance.setText("Attendance: " + student.getAttendancePercentage() + "%");
                    binding.tvKyc.setText("KYC: " + student.getKycStatus());
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

    private String getGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        if (hour < 21) return "Good Evening";
        return "Good Night";
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
