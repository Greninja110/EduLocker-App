package com.example.edulocker.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edulocker.databinding.ItemApplicationReviewBinding;
import com.example.edulocker.models.ScholarshipApplication;

import java.util.List;

public class ApplicationReviewAdapter extends RecyclerView.Adapter<ApplicationReviewAdapter.ViewHolder> {

    public interface OnApplicationAction {
        void onAction(ScholarshipApplication application);
    }

    private final List<ScholarshipApplication> items;
    private final OnApplicationAction approveListener;
    private final OnApplicationAction rejectListener;

    public ApplicationReviewAdapter(List<ScholarshipApplication> items,
                                    OnApplicationAction approveListener,
                                    OnApplicationAction rejectListener) {
        this.items = items;
        this.approveListener = approveListener;
        this.rejectListener = rejectListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemApplicationReviewBinding binding = ItemApplicationReviewBinding.inflate(
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
        private final ItemApplicationReviewBinding binding;

        ViewHolder(ItemApplicationReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ScholarshipApplication app) {
            binding.tvStudentName.setText(app.getStudentName());
            binding.tvPassportId.setText(app.getPassportId());
            binding.tvClassCategory.setText((app.getStudentClass() != null ? app.getStudentClass() : "")
                    + " • " + (app.getStudentCategory() != null ? app.getStudentCategory() : ""));
            binding.tvStatus.setText(app.getStatus());

            int statusColor;
            switch (app.getStatus() != null ? app.getStatus() : "") {
                case ScholarshipApplication.STATUS_APPROVED: statusColor = 0xFF2E7D32; break;
                case ScholarshipApplication.STATUS_REJECTED: statusColor = 0xFFB71C1C; break;
                default: statusColor = 0xFF1565C0; break;
            }
            binding.tvStatus.setTextColor(statusColor);

            boolean isPending = ScholarshipApplication.STATUS_SUBMITTED.equals(app.getStatus())
                    || ScholarshipApplication.STATUS_UNDER_REVIEW.equals(app.getStatus());
            binding.btnApprove.setVisibility(isPending ? android.view.View.VISIBLE : android.view.View.GONE);
            binding.btnReject.setVisibility(isPending ? android.view.View.VISIBLE : android.view.View.GONE);

            binding.btnApprove.setOnClickListener(v -> approveListener.onAction(app));
            binding.btnReject.setOnClickListener(v -> rejectListener.onAction(app));

            if (app.getReviewNote() != null && !app.getReviewNote().isEmpty()) {
                binding.tvReviewNote.setText("Note: " + app.getReviewNote());
                binding.tvReviewNote.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvReviewNote.setVisibility(android.view.View.GONE);
            }
        }
    }
}
