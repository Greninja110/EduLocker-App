package com.example.edulocker.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edulocker.databinding.ItemSchoolBinding;
import com.example.edulocker.models.School;

import java.util.List;

public class SchoolAdapter extends RecyclerView.Adapter<SchoolAdapter.ViewHolder> {

    public interface OnSchoolClickListener {
        void onClick(School school);
    }

    private final List<School> schools;
    private final OnSchoolClickListener listener;

    public SchoolAdapter(List<School> schools, OnSchoolClickListener listener) {
        this.schools = schools;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSchoolBinding binding = ItemSchoolBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(schools.get(position));
    }

    @Override
    public int getItemCount() { return schools.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSchoolBinding binding;

        ViewHolder(ItemSchoolBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(School school) {
            binding.tvSchoolName.setText(school.getName());
            binding.tvDistrict.setText(school.getDistrict() + " • " + school.getType());
            binding.tvSchoolCode.setText("Code: " + school.getSchoolCode());
            binding.tvStudentCount.setText(school.getStudentCount() + " students");
            binding.getRoot().setOnClickListener(v -> listener.onClick(school));
        }
    }
}
