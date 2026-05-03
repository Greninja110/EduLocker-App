package com.example.edulocker.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edulocker.databinding.ItemApplicationStatusBinding;
import com.example.edulocker.models.ScholarshipApplication;

import java.util.List;

public class ApplicationStatusAdapter extends RecyclerView.Adapter<ApplicationStatusAdapter.ViewHolder> {

    private final List<ScholarshipApplication> items;

    public ApplicationStatusAdapter(List<ScholarshipApplication> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemApplicationStatusBinding binding = ItemApplicationStatusBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemApplicationStatusBinding binding;

        ViewHolder(ItemApplicationStatusBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ScholarshipApplication app) {
            binding.tvScholarshipTitle.setText(app.getScholarshipTitle());
            binding.tvStatus.setText(app.getStatus());

            int statusColor;
            switch (app.getStatus() != null ? app.getStatus() : "") {
                case ScholarshipApplication.STATUS_APPROVED: statusColor = 0xFF2E7D32; break;
                case ScholarshipApplication.STATUS_REJECTED: statusColor = 0xFFB71C1C; break;
                default: statusColor = 0xFF1565C0; break;
            }
            binding.tvStatus.setTextColor(statusColor);

            if (app.getReviewNote() != null && !app.getReviewNote().isEmpty()) {
                binding.tvNote.setText(app.getReviewNote());
                binding.tvNote.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvNote.setVisibility(android.view.View.GONE);
            }
        }
    }
}
