package com.example.edulocker.activities.govt;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.edulocker.adapters.DocTypeAdapter;
import com.example.edulocker.databinding.ActivityGovDocTypesBinding;
import com.example.edulocker.models.DocumentType;
import com.example.edulocker.repositories.DocumentTypeRepository;

import java.util.ArrayList;
import java.util.List;

public class GovDocTypesActivity extends AppCompatActivity {

    private ActivityGovDocTypesBinding binding;
    private DocTypeAdapter adapter;
    private final List<DocumentType> docTypes = new ArrayList<>();
    private final DocumentTypeRepository repo = new DocumentTypeRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGovDocTypesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, top, 0, 0);
            return insets;
        });
        binding.btnBack.setOnClickListener(v -> finish());

        binding.rvDocTypes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DocTypeAdapter(docTypes, this::onDeleteType);
        binding.rvDocTypes.setAdapter(adapter);

        binding.fabAddDocType.setOnClickListener(v -> showAddDialog());
        loadDocTypes();
    }

    private void loadDocTypes() {
        binding.progressBar.setVisibility(View.VISIBLE);
        repo.getDocumentTypes(new DocumentTypeRepository.DocTypeListCallback() {
            @Override
            public void onSuccess(List<DocumentType> types) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    docTypes.clear();
                    docTypes.addAll(types);
                    adapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(docTypes.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(GovDocTypesActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(
                com.example.edulocker.R.layout.dialog_add_doc_type, null);
        com.google.android.material.textfield.TextInputEditText etName =
                dialogView.findViewById(com.example.edulocker.R.id.et_doc_type_name);
        android.widget.CheckBox cbMandatory =
                dialogView.findViewById(com.example.edulocker.R.id.cb_mandatory);

        new AlertDialog.Builder(this)
                .setTitle("Add Document Type")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, w) -> {
                    String name = etName.getText() != null
                            ? etName.getText().toString().trim() : "";
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Enter a name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    repo.addDocumentType(name, cbMandatory.isChecked(),
                            new DocumentTypeRepository.DocTypeCallback() {
                                @Override public void onSuccess() { runOnUiThread(() -> loadDocTypes()); }
                                @Override public void onFailure(String e) {
                                    runOnUiThread(() -> Toast.makeText(GovDocTypesActivity.this,
                                            "Error: " + e, Toast.LENGTH_SHORT).show());
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void onDeleteType(DocumentType dt) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Document Type")
                .setMessage("Delete \"" + dt.getName() + "\"? This won't affect already uploaded documents.")
                .setPositiveButton("Delete", (d, w) ->
                        repo.deleteDocumentType(dt.getDocTypeId(),
                                new DocumentTypeRepository.DocTypeCallback() {
                                    @Override public void onSuccess() { runOnUiThread(() -> loadDocTypes()); }
                                    @Override public void onFailure(String e) {}
                                }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
