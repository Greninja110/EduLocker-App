package com.example.edulocker.fragments.teacher;

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
import com.example.edulocker.databinding.FragmentTeacherProfileBinding;
import com.example.edulocker.models.School;
import com.example.edulocker.repositories.SchoolRepository;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TeacherProfileFragment extends Fragment {

    private FragmentTeacherProfileBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTeacherProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SessionManager session = new SessionManager(requireContext());
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();

        String name = session.getUserName();
        String email = u != null ? u.getEmail() : "";
        String schoolId = session.getSchoolId();

        // Header card
        binding.tvUserName.setText(name != null ? name : "Faculty");
        binding.tvUserEmail.setText(email != null ? email : "");

        // Show overview card immediately with session data
        binding.cardFacultyOverview.setVisibility(View.VISIBLE);
        binding.tvOverviewOrg.setText("—");
        binding.tvOverviewSubject.setText("—");
        binding.tvOverviewClassCount.setText("—");
        binding.tvOverviewStudentCount.setText("—");

        // Load org name from school repo
        if (schoolId != null) {
            new SchoolRepository().getSchool(schoolId, new SchoolRepository.SchoolCallback() {
                @Override public void onSuccess(School school) {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding == null) return;
                        binding.tvOverviewOrg.setText(school.getName() != null ? school.getName() : "—");
                        binding.tvSchoolName.setText(school.getName() != null ? school.getName() : "");
                    });
                }
                @Override public void onFailure(String error) {}
            });
        }

        loadTeacherInfo(u != null ? u.getUid() : null, email, schoolId);

        binding.ivSettings.setOnClickListener(v ->
                startActivity(new Intent(getContext(), SettingsActivity.class)));

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            session.clearSession();
            startActivity(new Intent(getContext(), LoginActivity.class));
            requireActivity().finishAffinity();
        });

        binding.swipeRefresh.setColorSchemeResources(
                com.example.edulocker.R.color.primary, com.example.edulocker.R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            FirebaseUser cur = FirebaseAuth.getInstance().getCurrentUser();
            loadTeacherInfo(cur != null ? cur.getUid() : null,
                    cur != null ? cur.getEmail() : null, schoolId);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        String schoolId = new SessionManager(requireContext()).getSchoolId();
        loadTeacherInfo(u != null ? u.getUid() : null,
                u != null ? u.getEmail() : null, schoolId);
    }

    private void loadTeacherInfo(String uid, String email, String schoolId) {
        if ((uid == null && email == null) || !isAdded() || binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        // Try by userId first; fallback to loginEmail
        com.google.firebase.firestore.Query query = uid != null
                ? Constants.db().collection(Constants.COL_TEACHERS).whereEqualTo("userId", uid).limit(1)
                : Constants.db().collection(Constants.COL_TEACHERS).whereEqualTo("loginEmail", email).limit(1);

        query.get()
                .addOnSuccessListener(snap -> {
                    if (!isAdded() || binding == null) return;

                    // If userId query returned empty, retry by loginEmail
                    if (snap.isEmpty() && uid != null && email != null) {
                        Constants.db().collection(Constants.COL_TEACHERS)
                                .whereEqualTo("loginEmail", email)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(snap2 -> populateFromSnap(snap2, schoolId))
                                .addOnFailureListener(e -> dismissProgress());
                        return;
                    }
                    populateFromSnap(snap, schoolId);
                })
                .addOnFailureListener(e -> dismissProgress());
    }

    private void populateFromSnap(com.google.firebase.firestore.QuerySnapshot snap, String schoolId) {
        if (!isAdded() || binding == null) return;
        requireActivity().runOnUiThread(() -> {
            if (binding == null) return;
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);

            if (snap.isEmpty()) return;

            com.google.firebase.firestore.DocumentSnapshot doc = snap.getDocuments().get(0);
            String teacherId    = doc.getId();
            String subject      = doc.getString("subject");
            String assignedClass = doc.getString("assignedClass");
            String phone        = doc.getString("phone");
            String schoolName   = doc.getString("schoolName");
            String docSchoolId  = doc.getString("schoolId");
            String effectiveSchoolId = docSchoolId != null ? docSchoolId : schoolId;

            // Populate merged Faculty Overview card
            binding.tvTeacherId.setText(teacherId != null ? teacherId : "—");
            binding.tvOverviewSubject.setText(subject != null ? subject : "—");
            binding.tvAssignedClass.setText(assignedClass != null ? assignedClass : "—");
            binding.tvPhone.setText(phone != null ? phone : "—");
            if (schoolName != null) {
                binding.tvSchoolName.setText(schoolName);
                binding.tvOverviewOrg.setText(schoolName);
            }

            // Build class list
            List<String> classList = new ArrayList<>();
            if (assignedClass != null && !assignedClass.isEmpty()) classList.add(assignedClass);
            try {
                Object raw = doc.get("classAssignments");
                if (raw instanceof List) {
                    for (Object item : (List<?>) raw) {
                        if (item instanceof Map) {
                            Object cls = ((Map<?, ?>) item).get("assignedClass");
                            if (cls instanceof String && !classList.contains(cls))
                                classList.add((String) cls);
                        }
                    }
                }
            } catch (Exception ignored) {}

            binding.tvOverviewClassCount.setText(String.valueOf(classList.size()));

            if (effectiveSchoolId != null && !classList.isEmpty()) {
                Constants.db().collection(Constants.COL_STUDENTS)
                        .whereEqualTo("schoolId", effectiveSchoolId)
                        .whereIn("studentClass", classList)
                        .count().get(AggregateSource.SERVER)
                        .addOnSuccessListener(agg -> {
                            if (!isAdded() || binding == null) return;
                            requireActivity().runOnUiThread(() -> {
                                if (binding != null)
                                    binding.tvOverviewStudentCount.setText(String.valueOf(agg.getCount()));
                            });
                        });
            } else {
                binding.tvOverviewStudentCount.setText("0");
            }
        });
    }

    private void dismissProgress() {
        if (!isAdded() || binding == null) return;
        requireActivity().runOnUiThread(() -> {
            if (binding == null) return;
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
