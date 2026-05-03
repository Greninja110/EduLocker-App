package com.example.edulocker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.edulocker.adapters.NewsAdapter;
import com.example.edulocker.databinding.FragmentNewsBinding;
import com.example.edulocker.models.NewsItem;
import com.example.edulocker.repositories.NewsRepository;
import com.example.edulocker.utils.SessionManager;

import java.util.List;

public class NewsFragment extends Fragment {

    private FragmentNewsBinding binding;
    private String activeCategory = null; // null = all

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNewsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String studentName = new SessionManager(requireContext()).getUserName();
        binding.tvWelcome.setText("Welcome back");
        binding.tvStudentName.setText(studentName != null ? studentName : "");

        binding.rvNews.setLayoutManager(new LinearLayoutManager(getContext()));

        // Chip → NewsData.io category string (null = show all)
        binding.chipAll.setOnClickListener(v      -> loadNews(null));
        binding.chipGovt.setOnClickListener(v     -> loadNews("education"));
        binding.chipOdishaX.setOnClickListener(v  -> loadNews("politics"));
        binding.chipBise.setOnClickListener(v     -> loadNews("top"));
        binding.chipGovtIndia.setOnClickListener(v -> loadNews("science"));

        loadNews(null);
    

        binding.swipeRefresh.setColorSchemeResources(
                com.example.edulocker.R.color.primary, com.example.edulocker.R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(() ->
                loadNews(activeCategory));
    }

    private void loadNews(String categoryFilter) {
        activeCategory = categoryFilter;
        if (!isAdded() || binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        new NewsRepository().getNews(requireContext(), categoryFilter,
                new NewsRepository.NewsListCallback() {
                    @Override
                    public void onSuccess(List<NewsItem> items) {
                        if (!isAdded() || binding == null) return;
                        requireActivity().runOnUiThread(() -> {
                            if (binding == null) return;
                            binding.progressBar.setVisibility(View.GONE);
                    if (binding != null) binding.swipeRefresh.setRefreshing(false);
                            binding.rvNews.setAdapter(new NewsAdapter(items));
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        if (!isAdded() || binding == null) return;
                        requireActivity().runOnUiThread(() -> {
                            if (binding == null) return;
                            binding.progressBar.setVisibility(View.GONE);
                    if (binding != null) binding.swipeRefresh.setRefreshing(false);
                        });
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
