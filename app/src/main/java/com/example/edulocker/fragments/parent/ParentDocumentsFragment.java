package com.example.edulocker.fragments.parent;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.edulocker.activities.school.UploadDocumentActivity;
import com.example.edulocker.adapters.DocumentAdapter;
import com.example.edulocker.databinding.FragmentParentDocumentsBinding;
import com.example.edulocker.models.EduDocument;
import com.example.edulocker.models.Student;
import com.example.edulocker.repositories.DocumentRepository;
import com.example.edulocker.repositories.StudentRepository;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ParentDocumentsFragment extends Fragment {

    private FragmentParentDocumentsBinding binding;
    private String childPassportId;
    private String currentUserId;
    private final List<EduDocument> documentList = new ArrayList<>();
    private DocumentAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentParentDocumentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = user != null ? user.getUid() : null;

        binding.rvDocuments.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.fabUpload.setOnClickListener(v -> openUpload());

        loadDocuments();
    

        binding.swipeRefresh.setColorSchemeResources(
                com.example.edulocker.R.color.primary, com.example.edulocker.R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadDocuments();
        });
    }

    private void openUpload() {
        if (childPassportId == null) {
            Toast.makeText(getContext(), "Child data still loading, try again", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getContext(), UploadDocumentActivity.class);
        intent.putExtra(Constants.EXTRA_PASSPORT_ID, childPassportId);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload after returning from upload
        if (childPassportId != null) loadDocumentsForChild(childPassportId);
    }

    private void loadDocuments() {
        String uid = new SessionManager(requireContext()).getUserUid();
        if (uid == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        new StudentRepository().getStudentByParentUserId(uid, new StudentRepository.StudentCallback() {
            @Override public void onSuccess(Student student) {
                if (!isAdded() || binding == null) return;
                childPassportId = student.getPassportId();
                loadDocumentsForChild(childPassportId);
            }
            @Override public void onFailure(String error) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    if (binding != null) binding.swipeRefresh.setRefreshing(false);
                });
            }
        });
    }

    private void loadDocumentsForChild(String passportId) {
        if (!isAdded() || binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        new DocumentRepository().getDocumentsForStudent(passportId,
                new DocumentRepository.DocumentListCallback() {
                    @Override public void onSuccess(List<EduDocument> documents) {
                        if (!isAdded() || binding == null) return;
                        requireActivity().runOnUiThread(() -> {
                            if (binding == null) return;
                            binding.progressBar.setVisibility(View.GONE);
                    if (binding != null) binding.swipeRefresh.setRefreshing(false);
                            documentList.clear();
                            documentList.addAll(documents);

                            if (documentList.isEmpty()) {
                                binding.tvEmpty.setVisibility(View.VISIBLE);
                                binding.rvDocuments.setAdapter(null);
                            } else {
                                binding.tvEmpty.setVisibility(View.GONE);
                                // Parent can delete only documents they uploaded (currentUserId filter)
                                adapter = new DocumentAdapter(
                                        documentList,
                                        doc -> openDoc(doc),
                                        (doc, pos) -> confirmDelete(doc, pos),
                                        currentUserId
                                );
                                binding.rvDocuments.setAdapter(adapter);
                            }
                        });
                    }
                    @Override public void onFailure(String error) {
                        if (!isAdded() || binding == null) return;
                        requireActivity().runOnUiThread(() -> {
                            if (binding == null) return;
                            binding.progressBar.setVisibility(View.GONE);
                    if (binding != null) binding.swipeRefresh.setRefreshing(false);
                        });
                    }
                });
    }

    private void openDoc(EduDocument document) {
        if (document.getFileUrl() != null && !document.getFileUrl().isEmpty())
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(document.getFileUrl())));
    }

    private void confirmDelete(EduDocument doc, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Document")
                .setMessage("Delete \"" + doc.getTitle() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> deleteDoc(doc, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteDoc(EduDocument doc, int position) {
        new DocumentRepository().deleteDocument(doc.getDocId(),
                new DocumentRepository.DocumentDeleteCallback() {
                    @Override public void onSuccess() {
                        if (!isAdded() || binding == null) return;
                        requireActivity().runOnUiThread(() -> {
                            if (adapter != null) adapter.removeAt(position);
                            if (documentList.isEmpty())
                                binding.tvEmpty.setVisibility(View.VISIBLE);
                        });
                    }
                    @Override public void onFailure(String error) {
                        if (!isAdded() || binding == null) return;
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Delete failed: " + error,
                                        Toast.LENGTH_SHORT).show());
                    }
                });
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
