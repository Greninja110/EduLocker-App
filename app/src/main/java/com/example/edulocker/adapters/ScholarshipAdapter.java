package com.example.edulocker.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edulocker.databinding.ItemScholarshipBinding;
import com.example.edulocker.models.Scholarship;

import java.util.List;

public class ScholarshipAdapter extends RecyclerView.Adapter<ScholarshipAdapter.ViewHolder> {

    public interface OnScholarshipClickListener {
        void onClick(Scholarship scholarship);
    }

    public interface OnScholarshipReviewListener {
        void onReview(Scholarship scholarship);
    }

    private final List<Scholarship> scholarships;
    private final OnScholarshipClickListener listener;
    private OnScholarshipReviewListener reviewListener;

    public ScholarshipAdapter(List<Scholarship> scholarships, OnScholarshipClickListener listener) {
        this.scholarships = scholarships;
        this.listener = listener;
    }

    public void setReviewListener(OnScholarshipReviewListener reviewListener) {
        this.reviewListener = reviewListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemScholarshipBinding binding = ItemScholarshipBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(scholarships.get(position));
    }

    @Override
    public int getItemCount() { return scholarships.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemScholarshipBinding binding;

        ViewHolder(ItemScholarshipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Scholarship s) {
            binding.tvTitle.setText(s.getTitle());
            binding.tvIssuedBy.setText(s.getIssuedBy());
            binding.tvAmount.setText(s.getAmount());
            binding.tvDeadline.setText("Deadline: " + s.getDeadline());
            binding.btnApply.setOnClickListener(v -> listener.onClick(s));

            if (reviewListener != null) {
                binding.btnApply.setText("View Applications");
                binding.btnApply.setOnClickListener(v -> reviewListener.onReview(s));
            }
        }
    }
}
