package com.example.edulocker.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edulocker.activities.school.UploadDocumentActivity;
import com.example.edulocker.databinding.ItemRequiredDocStatusBinding;
import com.example.edulocker.models.EduDocument;

import java.util.List;

public class RequiredDocStatusAdapter extends RecyclerView.Adapter<RequiredDocStatusAdapter.ViewHolder> {

    public static class DocStatus {
        public String docTypeName;
        public boolean mandatory;
        public EduDocument document; // null = not uploaded
    }

    private final List<DocStatus> items;
    private final Context context;

    public RequiredDocStatusAdapter(List<DocStatus> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRequiredDocStatusBinding binding = ItemRequiredDocStatusBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemRequiredDocStatusBinding binding;

        ViewHolder(ItemRequiredDocStatusBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DocStatus status) {
            String label = status.docTypeName + (status.mandatory ? " *" : " (optional)");
            binding.tvDocName.setText(label);

            if (status.document != null) {
                // Document found
                binding.tvDocStatus.setText("✓ Found");
                binding.tvDocStatus.setTextColor(0xFF2E7D32); // dark green
                binding.btnUploadDoc.setVisibility(android.view.View.GONE);
            } else if (status.mandatory) {
                // Missing mandatory
                binding.tvDocStatus.setText("✗ Missing (required)");
                binding.tvDocStatus.setTextColor(0xFFB71C1C); // dark red
                binding.btnUploadDoc.setVisibility(android.view.View.VISIBLE);
                binding.btnUploadDoc.setText("Upload Required");
                binding.btnUploadDoc.setOnClickListener(v -> {
                    Intent intent = new Intent(context, UploadDocumentActivity.class);
                    context.startActivity(intent);
                });
            } else {
                // Missing optional
                binding.tvDocStatus.setText("— Missing (optional)");
                binding.tvDocStatus.setTextColor(0xFFF57F17); // amber
                binding.btnUploadDoc.setVisibility(android.view.View.VISIBLE);
                binding.btnUploadDoc.setText("Upload Optional");
                binding.btnUploadDoc.setOnClickListener(v -> {
                    Intent intent = new Intent(context, UploadDocumentActivity.class);
                    context.startActivity(intent);
                });
            }
        }
    }
}
