package com.example.edulocker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.edulocker.activities.OrgInfoActivity;
import com.example.edulocker.adapters.DocumentAdapter;
import com.example.edulocker.adapters.SchoolAdapter;
import com.example.edulocker.databinding.FragmentSearchBinding;
import com.example.edulocker.models.EduDocument;
import com.example.edulocker.models.School;
import com.example.edulocker.repositories.DocumentRepository;
import com.example.edulocker.repositories.SchoolRepository;
import com.example.edulocker.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements DocumentAdapter.DocumentClickListener {

    private FragmentSearchBinding binding;
    private List<EduDocument> allDocuments = new ArrayList<>();
    private List<School> allSchools = new ArrayList<>();
    private boolean showingDocuments = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvResults.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSchools.setLayoutManager(new LinearLayoutManager(getContext()));
        loadAllData();
        setupSearch();
        setupFilterChips();

        binding.swipeRefresh.setColorSchemeResources(
                com.example.edulocker.R.color.primary, com.example.edulocker.R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(this::loadAllData);
    }

    private void loadAllData() {
        SessionManager session = new SessionManager(requireContext());
        String passportId = session.getPassportId();
        String viewerUid  = session.getUserUid();
        String viewerRole = session.getUserRole();

        if (passportId != null) {
            new DocumentRepository().getDocumentsForStudentWithPermission(
                    passportId, viewerUid, viewerRole,
                    new DocumentRepository.DocumentListCallback() {
                        @Override
                        public void onSuccess(List<EduDocument> documents) {
                            allDocuments = documents;
                            if (binding != null) {
                                binding.swipeRefresh.setRefreshing(false);
                                requireActivity().runOnUiThread(() -> refreshDisplay());
                            }
                        }
                        @Override
                        public void onFailure(String error) {
                            if (binding != null) binding.swipeRefresh.setRefreshing(false);
                        }
                    });
        }

        new SchoolRepository().getAllSchools(new SchoolRepository.SchoolListCallback() {
            @Override
            public void onSuccess(List<School> schools) {
                allSchools = schools;
                if (binding != null) requireActivity().runOnUiThread(() -> refreshDisplay());
            }
            @Override
            public void onFailure(String error) {}
        });
    }

    private void setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            showingDocuments = checkedIds.get(0) == com.example.edulocker.R.id.chip_documents;
            refreshDisplay();
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshDisplay();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void refreshDisplay() {
        if (binding == null) return;
        String query = binding.etSearch.getText() != null
                ? binding.etSearch.getText().toString().trim().toLowerCase()
                : "";

        if (showingDocuments) {
            showDocuments(query);
        } else {
            showOrganisations(query);
        }
    }

    private void showDocuments(String query) {
        binding.tvSectionLabel.setText("My Documents");
        binding.rvResults.setVisibility(View.VISIBLE);
        binding.rvSchools.setVisibility(View.GONE);

        List<EduDocument> filtered = new ArrayList<>();
        for (EduDocument doc : allDocuments) {
            if (query.isEmpty()) {
                filtered.add(doc);
            } else {
                String title = doc.getDisplayTitle();
                String type  = doc.getType();
                if ((title != null && title.toLowerCase().contains(query))
                        || (type != null && type.toLowerCase().contains(query))) {
                    filtered.add(doc);
                }
            }
        }

        binding.tvResultCount.setText(filtered.size() + " document(s)");
        binding.tvResultCount.setVisibility(View.VISIBLE);
        binding.rvResults.setAdapter(new DocumentAdapter(filtered, this));
    }

    private void showOrganisations(String query) {
        binding.tvSectionLabel.setText("Organisations");
        binding.rvResults.setVisibility(View.GONE);
        binding.rvSchools.setVisibility(View.VISIBLE);
        binding.tvResultCount.setVisibility(View.GONE);

        List<School> filtered = new ArrayList<>();
        for (School s : allSchools) {
            if (query.isEmpty()) {
                filtered.add(s);
            } else if (s.getName() != null && s.getName().toLowerCase().contains(query)) {
                filtered.add(s);
            }
        }

        binding.rvSchools.setAdapter(new SchoolAdapter(filtered, school -> {
            Intent intent = new Intent(getContext(), OrgInfoActivity.class);
            intent.putExtra(OrgInfoActivity.EXTRA_SCHOOL_ID, school.getSchoolId());
            startActivity(intent);
        }));
    }

    @Override
    public void onDocumentClick(EduDocument document) {
        if (document.getFileUrl() != null && !document.getFileUrl().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse(document.getFileUrl()));
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
