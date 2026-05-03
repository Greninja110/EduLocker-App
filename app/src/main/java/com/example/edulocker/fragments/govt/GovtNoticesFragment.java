package com.example.edulocker.fragments.govt;

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
import com.example.edulocker.databinding.FragmentGovtNoticesBinding;
import com.example.edulocker.models.Notice;
import com.example.edulocker.models.Scholarship;
import com.example.edulocker.repositories.NoticeRepository;
import com.example.edulocker.repositories.ScholarshipRepository;
import com.example.edulocker.utils.Constants;

import java.util.List;

public class GovtNoticesFragment extends Fragment {

    private FragmentGovtNoticesBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGovtNoticesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvNotices.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvScholarships.setLayoutManager(new LinearLayoutManager(getContext()));

        // Scholarships shown by default
        showScholarships();

        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_scholarships) {
                showScholarships();
            } else if (id == R.id.chip_notices) {
                showNotices();
            }
        });

        binding.fabPostNotice.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), PostNoticeActivity.class);
            intent.putExtra(PostNoticeActivity.EXTRA_NOTICE_TYPE, "state");
            startActivity(intent);
        });

        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (binding.chipNotices.isChecked()) loadNotices(); else loadScholarships();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null) return;
        if (binding.chipNotices.isChecked()) loadNotices(); else loadScholarships();
    }

    private void showScholarships() {
        if (binding == null) return;
        binding.sectionScholarships.setVisibility(View.VISIBLE);
        binding.sectionNotices.setVisibility(View.GONE);
        loadScholarships();
    }

    private void showNotices() {
        if (binding == null) return;
        binding.sectionScholarships.setVisibility(View.GONE);
        binding.sectionNotices.setVisibility(View.VISIBLE);
        loadNotices();
    }

    private void loadScholarships() {
        if (!isAdded() || binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        new ScholarshipRepository().getAllScholarships(new ScholarshipRepository.ScholarshipListCallback() {
            @Override
            public void onSuccess(List<Scholarship> scholarships) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    binding.tvScholarshipsLabel.setVisibility(
                            scholarships.isEmpty() ? View.GONE : View.VISIBLE);
                    ScholarshipAdapter adapter = new ScholarshipAdapter(scholarships, s -> {});
                    adapter.setReviewListener(scholarship -> {
                        Intent intent = new Intent(getContext(), ApplicationReviewActivity.class);
                        intent.putExtra(Constants.EXTRA_SCHOLARSHIP_ID, scholarship.getScholarshipId());
                        intent.putExtra(Constants.EXTRA_SCHOLARSHIP_TITLE, scholarship.getTitle());
                        startActivity(intent);
                    });
                    binding.rvScholarships.setAdapter(adapter);
                });
            }

            @Override
            public void onFailure(String error) {
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
        binding.progressBar.setVisibility(View.VISIBLE);

        new NoticeRepository().getAllNotices(new NoticeRepository.NoticeListCallback() {
            @Override
            public void onSuccess(List<Notice> notices) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    binding.tvEmpty.setVisibility(notices.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.rvNotices.setAdapter(new NoticeAdapter(notices,
                            new NoticeAdapter.NoticeActionListener() {
                        @Override
                        public void onEdit(Notice notice) {
                            Intent intent = new Intent(getContext(), PostNoticeActivity.class);
                            intent.putExtra(PostNoticeActivity.EXTRA_NOTICE_TYPE, notice.getNoticeType());
                            intent.putExtra(PostNoticeActivity.EXTRA_NOTICE_ID, notice.getNoticeId());
                            intent.putExtra(PostNoticeActivity.EXTRA_NOTICE_TITLE, notice.getTitle());
                            intent.putExtra(PostNoticeActivity.EXTRA_NOTICE_BODY, notice.getContent());
                            intent.putExtra(PostNoticeActivity.EXTRA_NOTICE_STATE, notice.getState());
                            startActivity(intent);
                        }

                        @Override
                        public void onDelete(Notice notice) {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Delete Notice")
                                    .setMessage("Delete \"" + notice.getTitle() + "\"?")
                                    .setPositiveButton("Delete", (d, w) ->
                                            new NoticeRepository().deleteNotice(
                                                    notice.getNoticeId(),
                                                    new NoticeRepository.NoticeCallback() {
                                                        @Override public void onSuccess() {
                                                            if (isAdded()) requireActivity()
                                                                    .runOnUiThread(() -> loadNotices());
                                                        }
                                                        @Override public void onFailure(String e) {}
                                                    }))
                                    .setNegativeButton("Cancel", null)
                                    .show();
                        }
                    }));
                });
            }

            @Override
            public void onFailure(String error) {
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
