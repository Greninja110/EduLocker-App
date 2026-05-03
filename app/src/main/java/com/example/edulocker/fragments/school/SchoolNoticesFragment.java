package com.example.edulocker.fragments.school;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.edulocker.R;
import com.example.edulocker.activities.ApplicationReviewActivity;
import com.example.edulocker.activities.school.PostNoticeActivity;
import com.example.edulocker.adapters.NoticeAdapter;
import com.example.edulocker.adapters.ScholarshipAdapter;
import com.example.edulocker.databinding.FragmentSchoolNoticesBinding;
import com.example.edulocker.models.Notice;
import com.example.edulocker.models.Scholarship;
import com.example.edulocker.repositories.NoticeRepository;
import com.example.edulocker.repositories.ScholarshipRepository;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.SessionManager;

import java.util.List;

public class SchoolNoticesFragment extends Fragment {

    private FragmentSchoolNoticesBinding binding;
    private boolean showingScholarships = true;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSchoolNoticesBinding.inflate(inflater, container, false);
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

        binding.fabPostNotice.setOnClickListener(v ->
                startActivity(new Intent(getContext(), PostNoticeActivity.class)));

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
        String schoolId = new SessionManager(requireContext()).getSchoolId();
        if (schoolId == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        new ScholarshipRepository().getScholarshipsForSchool(schoolId,
                new ScholarshipRepository.ScholarshipListCallback() {
            @Override public void onSuccess(List<Scholarship> list) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    ScholarshipAdapter adapter = new ScholarshipAdapter(list, s -> {});
                    adapter.setReviewListener(s -> {
                        Intent i = new Intent(getContext(), ApplicationReviewActivity.class);
                        i.putExtra(Constants.EXTRA_SCHOLARSHIP_ID, s.getScholarshipId());
                        i.putExtra(Constants.EXTRA_SCHOLARSHIP_TITLE, s.getTitle());
                        startActivity(i);
                    });
                    binding.rvScholarships.setAdapter(adapter);
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

        new NoticeRepository().getNoticesForSchool(schoolId, new NoticeRepository.NoticeListCallback() {
            @Override public void onSuccess(List<Notice> notices) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    binding.tvEmpty.setVisibility(notices.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.rvNotices.setAdapter(new NoticeAdapter(notices,
                            new NoticeAdapter.NoticeActionListener() {
                        @Override public void onEdit(Notice notice) {
                            Intent i = new Intent(getContext(), PostNoticeActivity.class);
                            i.putExtra(PostNoticeActivity.EXTRA_NOTICE_TYPE, notice.getNoticeType());
                            i.putExtra(PostNoticeActivity.EXTRA_NOTICE_ID, notice.getNoticeId());
                            i.putExtra(PostNoticeActivity.EXTRA_NOTICE_TITLE, notice.getTitle());
                            i.putExtra(PostNoticeActivity.EXTRA_NOTICE_BODY, notice.getContent());
                            i.putExtra(PostNoticeActivity.EXTRA_NOTICE_STATE, notice.getState());
                            startActivity(i);
                        }
                        @Override public void onDelete(Notice notice) {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Delete Notice")
                                    .setMessage("Delete \"" + notice.getTitle() + "\"?")
                                    .setPositiveButton("Delete", (d, w) ->
                                            new NoticeRepository().deleteNotice(notice.getNoticeId(),
                                                    new NoticeRepository.NoticeCallback() {
                                                        @Override public void onSuccess() {
                                                            if (isAdded()) requireActivity()
                                                                    .runOnUiThread(() -> loadNotices());
                                                        }
                                                        @Override public void onFailure(String e) {}
                                                    }))
                                    .setNegativeButton("Cancel", null).show();
                        }
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
