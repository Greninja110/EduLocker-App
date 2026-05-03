package com.example.edulocker.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.edulocker.adapters.DocumentAdapter;
import com.example.edulocker.databinding.FragmentDocumentsBinding;
import com.example.edulocker.models.EduDocument;
import com.example.edulocker.repositories.DocumentRepository;
import com.example.edulocker.utils.SessionManager;
import com.example.edulocker.utils.Constants;

import java.util.List;

public class DocumentsFragment extends Fragment implements DocumentAdapter.DocumentClickListener {

    private FragmentDocumentsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDocumentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvDocuments.setLayoutManager(new LinearLayoutManager(getContext()));
        loadDocuments();
    

        binding.swipeRefresh.setColorSchemeResources(
                com.example.edulocker.R.color.primary, com.example.edulocker.R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadDocuments();
        });
    }

    private void loadDocuments() {
        String passportId = new SessionManager(requireContext()).getPassportId();
        if (passportId == null) return;

        SessionManager session = new SessionManager(requireContext());
        String viewerUid  = session.getUserUid();
        String viewerRole = session.getUserRole();

        binding.progressBar.setVisibility(View.VISIBLE);
        new DocumentRepository().getDocumentsForStudentWithPermission(passportId, viewerUid, viewerRole,
                new DocumentRepository.DocumentListCallback() {
                    @Override
                    public void onSuccess(List<EduDocument> documents) {
                        if (!isAdded() || binding == null) return;
                        requireActivity().runOnUiThread(() -> {
                            if (binding == null) return;
                            binding.progressBar.setVisibility(View.GONE);
                    if (binding != null) binding.swipeRefresh.setRefreshing(false);
                            if (documents.isEmpty()) {
                                binding.tvEmpty.setVisibility(View.VISIBLE);
                            } else {
                                binding.tvEmpty.setVisibility(View.GONE);
                                binding.rvDocuments.setAdapter(
                                        new DocumentAdapter(documents, DocumentsFragment.this));
                            }
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
    public void onDocumentClick(EduDocument document) {
        // Open document URL in browser
        if (document.getFileUrl() != null && !document.getFileUrl().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(document.getFileUrl()));
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
