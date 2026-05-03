package com.example.edulocker.fragments.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.edulocker.activities.school.AddStudentActivity;
import com.example.edulocker.activities.school.UploadDocumentActivity;
import com.example.edulocker.adapters.StudentListAdapter;
import com.example.edulocker.databinding.FragmentTeacherStudentsBinding;
import com.example.edulocker.models.ClassAssignment;
import com.example.edulocker.models.Student;
import com.example.edulocker.models.Teacher;
import com.example.edulocker.repositories.StudentRepository;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class TeacherStudentsFragment extends Fragment {

    private FragmentTeacherStudentsBinding binding;
    private final List<ClassAssignment> teacherClasses = new ArrayList<>();
    private int selectedClassIndex = 0;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTeacherStudentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvStudents.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.fabAddStudent.setOnClickListener(v ->
                startActivity(new Intent(getContext(), AddStudentActivity.class)));

        binding.swipeRefresh.setColorSchemeResources(
                com.example.edulocker.R.color.primary, com.example.edulocker.R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(this::loadTeacherThenStudents);

        loadTeacherThenStudents();
    }

    private void loadTeacherThenStudents() {
        if (!isAdded() || binding == null) return;
        String schoolId = new SessionManager(requireContext()).getSchoolId();
        if (schoolId == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) { binding.progressBar.setVisibility(View.GONE); return; }

        Constants.db().collection("teachers")
                .whereEqualTo("userId", uid).limit(1).get()
                .addOnSuccessListener(query -> {
                    if (!isAdded() || binding == null) return;

                    teacherClasses.clear();
                    if (!query.isEmpty()) {
                        teacherClasses.addAll(
                                ClassAssignment.fromDocSnapshot(query.getDocuments().get(0)));
                    }

                    requireActivity().runOnUiThread(() -> {
                        if (binding == null) return;
                        setupClassFilter(schoolId);
                    });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (binding == null) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                    });
                });
    }

    private void setupClassFilter(String schoolId) {
        if (teacherClasses.isEmpty()) {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            return;
        }

        List<String> labels = new ArrayList<>();
        for (ClassAssignment ca : teacherClasses) labels.add(ca.label());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, labels);
        binding.spinnerClassFilter.setAdapter(adapter);
        binding.spinnerClassFilter.setText(labels.get(0), false);
        selectedClassIndex = 0;

        binding.spinnerClassFilter.setOnClickListener(v -> binding.spinnerClassFilter.showDropDown());
        binding.spinnerClassFilter.setOnItemClickListener((parent, v, pos, id) -> {
            selectedClassIndex = pos;
            loadStudentsForSelectedClass(schoolId);
        });

        loadStudentsForSelectedClass(schoolId);
    }

    private void loadStudentsForSelectedClass(String schoolId) {
        if (!isAdded() || binding == null || selectedClassIndex >= teacherClasses.size()) return;
        ClassAssignment ca = teacherClasses.get(selectedClassIndex);

        binding.progressBar.setVisibility(View.VISIBLE);

        new StudentRepository().getStudentsByClassAndSection(
                schoolId, ca.getAssignedClass(), ca.getDivision(),
                new StudentRepository.StudentListCallback() {
                    @Override public void onSuccess(List<Student> students) {
                        if (!isAdded() || binding == null) return;
                        requireActivity().runOnUiThread(() -> {
                            if (binding == null) return;
                            binding.progressBar.setVisibility(View.GONE);
                            binding.swipeRefresh.setRefreshing(false);
                            binding.rvStudents.setAdapter(new StudentListAdapter(students, student -> {
                                Intent intent = new Intent(getContext(), UploadDocumentActivity.class);
                                intent.putExtra(Constants.EXTRA_PASSPORT_ID, student.getPassportId());
                                startActivity(intent);
                            }));
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
