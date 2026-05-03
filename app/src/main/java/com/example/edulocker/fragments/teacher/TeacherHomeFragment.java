package com.example.edulocker.fragments.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.edulocker.activities.school.AddStudentActivity;
import com.example.edulocker.activities.school.UploadDocumentActivity;
import com.example.edulocker.databinding.FragmentTeacherHomeBinding;
import com.example.edulocker.models.Teacher;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateSource;

import java.util.List;

public class TeacherHomeFragment extends Fragment {

    private FragmentTeacherHomeBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTeacherHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SessionManager session = new SessionManager(requireContext());
        String name = session.getUserName();
        binding.tvTeacherName.setText((name != null ? name : "Faculty") + " !!");
        binding.tvTimeGreeting.setText(getGreeting());

        binding.btnUploadDoc.setOnClickListener(v ->
                startActivity(new Intent(getContext(), UploadDocumentActivity.class)));
        binding.btnAddStudent.setOnClickListener(v ->
                startActivity(new Intent(getContext(), AddStudentActivity.class)));

        binding.swipeRefresh.setColorSchemeResources(
                com.example.edulocker.R.color.primary, com.example.edulocker.R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(this::loadStats);

        loadStats();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        String schoolId = new SessionManager(requireContext()).getSchoolId();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (schoolId == null || user == null) return;

        // Students in this school
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

        // Docs in this school
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

        // Classes assigned to this teacher
        Constants.db().collection(Constants.COL_TEACHERS)
                .whereEqualTo("userId", user.getUid())
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!isAdded() || binding == null) return;
                    int classCount = 0;
                    if (!snap.isEmpty()) {
                        Teacher teacher = snap.getDocuments().get(0).toObject(Teacher.class);
                        if (teacher != null) {
                            List<?> assignments = teacher.getClassAssignments();
                            if (assignments != null && !assignments.isEmpty()) {
                                classCount = assignments.size();
                            } else if (teacher.getAssignedClass() != null
                                    && !teacher.getAssignedClass().isEmpty()) {
                                classCount = 1;
                            }
                        }
                    }
                    final int count = classCount;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.tvClassCount.setText(String.valueOf(count));
                    });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.tvClassCount.setText("—");
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
