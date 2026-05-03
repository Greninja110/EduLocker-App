package com.example.edulocker.fragments.school;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.edulocker.R;
import com.example.edulocker.activities.school.StudentDetailActivity;
import com.example.edulocker.activities.school.TeacherDetailActivity;
import com.example.edulocker.adapters.PeopleListAdapter;
import com.example.edulocker.adapters.PeopleListAdapter.PersonItem;
import com.example.edulocker.databinding.FragmentSchoolStudentsBinding;
import com.example.edulocker.models.Student;
import com.example.edulocker.models.Teacher;
import com.example.edulocker.repositories.StudentRepository;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SchoolStudentsFragment extends Fragment {

    private static final int FILTER_ALL      = 0;
    private static final int FILTER_STUDENTS = 1;
    private static final int FILTER_FACULTY  = 2;

    private FragmentSchoolStudentsBinding binding;
    private PeopleListAdapter adapter;

    private final List<Student> allStudents = new ArrayList<>();
    private final List<Teacher> allTeachers = new ArrayList<>();
    private int currentFilter = FILTER_ALL;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSchoolStudentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvStudents.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PeopleListAdapter(new ArrayList<>(), new PeopleListAdapter.OnPersonClickListener() {
            @Override
            public void onStudentClick(Student student) {
                Intent intent = new Intent(getContext(), StudentDetailActivity.class);
                intent.putExtra(Constants.EXTRA_PASSPORT_ID, student.getPassportId());
                startActivity(intent);
            }
            @Override
            public void onTeacherClick(Teacher teacher) {
                Intent intent = new Intent(getContext(), TeacherDetailActivity.class);
                intent.putExtra(TeacherDetailActivity.EXTRA_NAME,       teacher.getName());
                intent.putExtra(TeacherDetailActivity.EXTRA_PHONE,      teacher.getPhone());
                intent.putExtra(TeacherDetailActivity.EXTRA_EMAIL,      teacher.getLoginEmail());
                intent.putExtra(TeacherDetailActivity.EXTRA_SCHOOL_ID,  teacher.getSchoolId());
                intent.putExtra(TeacherDetailActivity.EXTRA_TEACHER_ID, teacher.getTeacherId());
                startActivity(intent);
            }
        });
        binding.rvStudents.setAdapter(adapter);

        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(this::loadAll);

        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_filter_all)      setFilter(FILTER_ALL);
            else if (id == R.id.chip_filter_students) setFilter(FILTER_STUDENTS);
            else if (id == R.id.chip_filter_faculty)  setFilter(FILTER_FACULTY);
        });

        loadAll();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAll();
    }

    private void setFilter(int filter) {
        currentFilter = filter;
        applyFilter();
    }

    private void applyFilter() {
        List<PersonItem> items = new ArrayList<>();
        if (currentFilter == FILTER_ALL || currentFilter == FILTER_FACULTY) {
            for (Teacher t : allTeachers) items.add(new PersonItem(t));
        }
        if (currentFilter == FILTER_ALL || currentFilter == FILTER_STUDENTS) {
            for (Student s : allStudents) items.add(new PersonItem(s));
        }
        if (adapter != null) adapter.updateList(items);
    }

    private void loadAll() {
        if (!isAdded() || binding == null) return;
        String schoolId = new SessionManager(requireContext()).getSchoolId();
        if (schoolId == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);

        // Use a counter — show results once both fetches complete
        AtomicInteger pending = new AtomicInteger(2);

        // Fetch students
        new StudentRepository().getStudentsBySchool(schoolId,
                new StudentRepository.StudentListCallback() {
                    @Override public void onSuccess(List<Student> students) {
                        allStudents.clear();
                        allStudents.addAll(students);
                        if (pending.decrementAndGet() == 0) onBothLoaded();
                    }
                    @Override public void onFailure(String error) {
                        if (pending.decrementAndGet() == 0) onBothLoaded();
                    }
                });

        // Fetch teachers
        Constants.db().collection("teachers")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(query -> {
                    allTeachers.clear();
                    for (var doc : query.getDocuments()) {
                        Teacher t = doc.toObject(Teacher.class);
                        if (t != null) allTeachers.add(t);
                    }
                    if (pending.decrementAndGet() == 0) onBothLoaded();
                })
                .addOnFailureListener(e -> {
                    if (pending.decrementAndGet() == 0) onBothLoaded();
                });
    }

    private void onBothLoaded() {
        if (!isAdded() || binding == null) return;
        requireActivity().runOnUiThread(() -> {
            if (binding == null) return;
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            applyFilter();
        });
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
